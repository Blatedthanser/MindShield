package com.example.mindshield.domain.analysis

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.LongBuffer
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.roundToInt

// 数据类保持不变
data class AnalysisResult(
    val label: String,
    val triggerText: String // 触发该结果的具体文字片段
)

class MultilingualTextClassifier(private val context: Context) {

    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null

    // 词汇表
    private val vocab = HashMap<String, Int>()
    private val unkId = 100L
    private var realUnkId = 100L
    private var clsId = 101L
    private var sepId = 102L

    private val labels = listOf(
        "非常负面 (Very Negative)",
        "负面 (Negative)",
        "中性 (Neutral)",
        "正面 (Positive)",
        "非常正面 (Very Positive)"
    )

    init {
        try {
            loadVocab(context)
            val modelPath = copyAssetToCache(context, "model.onnx")
            env = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions()
            options.setIntraOpNumThreads(4)
            session = env?.createSession(modelPath, options)
            if (session != null) println("Session Loaded")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tokenize(text: String): LongArray {
        val tokens = ArrayList<String>()
        tokens.add("[CLS]")
        val cleanedText = preTokenize(text)
        val basicParts = cleanedText.trim().split("\\s+".toRegex())

        for (part in basicParts) {
            if (part.isEmpty()) continue
            if (vocab.containsKey(part)) {
                tokens.add(part)
                continue
            }
            var current = part
            var start = 0
            var foundAny = false
            while (start < current.length) {
                var end = current.length
                var subToken: String? = null
                while (end > start) {
                    var sub = current.substring(start, end)
                    if (start > 0) sub = "##$sub"
                    if (vocab.containsKey(sub)) {
                        subToken = sub
                        break
                    }
                    end--
                }
                if (subToken == null) {
                    tokens.add("[UNK]")
                    foundAny = true
                    break
                } else {
                    tokens.add(subToken)
                    start = end
                    foundAny = true
                }
            }
            if (!foundAny) tokens.add("[UNK]")
        }
        tokens.add("[SEP]")
        return tokens.map { vocab[it]?.toLong() ?: realUnkId }.toLongArray()
    }

    private fun preTokenize(text: String): String {
        val sb = StringBuilder()
        for (i in text.indices) {
            val c = text[i]
            if (isChineseChar(c) || isPunctuation(c)) {
                sb.append(" ").append(c).append(" ")
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun isChineseChar(c: Char): Boolean {
        return (c.code in 0x4E00..0x9FFF) ||
                (c.code in 0x3400..0x4DBF) ||
                (c.code in 0x20000..0x2A6DF) ||
                (c.code in 0x2A700..0x2B73F) ||
                (c.code in 0x2B740..0x2B81F) ||
                (c.code in 0x2B820..0x2CEAF) ||
                (c.code in 0xF900..0xFAFF) ||
                (c.code in 0x2F800..0x2FA1F)
    }

    private fun isPunctuation(c: Char): Boolean {
        if (c.isLetterOrDigit() || c.isWhitespace()) return false
        return true
    }

    // ================== 修改重点在 analyze 函数 ==================
    fun analyze(Rawtext: String): AnalysisResult {
        // 返回 AnalysisResult 而不是 String
        if (session == null || vocab.isEmpty()) {
            return AnalysisResult("Error", "模型未加载")
        }

        val text = cleanSocialMediaText(Rawtext)
        println("==============Cleaned Text==============\n $text \n\n\n")

        try {
            // 这里不需要调用 tokenize 了，因为后面 predictSingleWindow 会调
            // val tokenIds = tokenize(text)

            var sentences = text.split(Regex("[!?;。\n！？；]"))
            sentences = sentences.filter{ it.length > 2 && !it.matches(Regex("^[0-9]+$")) }

            // Case 1: 文本太短，直接分析全文
            if (sentences.size < 3) {
                val idx = predictSingleWindow(text)
                // 返回对象，triggerText 就是全文
                return AnalysisResult(labels[idx], text)
            }

            var count = intArrayOf(0,0,0,0,0)
            var minIndex = 10 // 初始化

            // 用于记录最糟糕的那个窗口文字
            var worstWindowText = ""

            // Sliding Window Loop
            for (i in 0 until sentences.size - 2) {
                val windowText = "${sentences[i]}, ${sentences[i+1]}, ${sentences[i+2]}"
                val index = predictSingleWindow(windowText)

                // 如果发现了更负面的情绪，更新 minIndex 并保存这段文字
                if (index < minIndex) {
                    minIndex = index
                    worstWindowText = windowText // <--- 关键修改：保存罪魁祸首
                }

                if (index == 0) println("Very Negative: $windowText")
                else if (index == 1) println("Negative: $windowText")
                count[index] ++
            }

            println("minIndex: $minIndex, count info...")

            // 如果 minIndex 还是初始值，说明没有有效窗口，或者没找到负面
            // 返回兜底，triggerText 取开头60字
            if (minIndex == 10 || minIndex == 100) {
                return AnalysisResult(labels[2], text.take(60))
            }

            // Case 2: 非常负面
            // 返回 worstWindowText
            if (minIndex == 0) {
                return AnalysisResult(labels[0], worstWindowText)
            }

            // 计算加权总数
            val totalCount = 1.5*count[1] + count[2] + 0.8*count[3] + 1.2*count[4]

            // Case 3: 负面但可能被中和
            if (minIndex == 1) {
                val weighedsum = ((1.5*1*count[1] + 2*count[2] + 0.8*3*count[3] + 1.2*4*count[4])/totalCount)
                println("weighedsum = $weighedsum")
                // 返回计算后的标签，但 triggerText 依然是那个触发了 "1 (Negative)" 的窗口
                return AnalysisResult(labels[weighedsum.roundToInt()], worstWindowText)
            }

            // Case 4: 其他情况 (中性/正面)
            val finalIdx = (2*count[2] + 3*count[3] + 4*count[4]) / (count[2] + count[3] + count[4])
            // 如果是正面结果，triggerText 可能不太重要，返回最“坏”的或者开头都可以
            // 这里为了统一，如果 worstWindowText 有值就返回它，否则返回开头
            val finalSnippet = if (worstWindowText.isNotEmpty()) worstWindowText else text.take(60)

            return AnalysisResult(labels[finalIdx], finalSnippet)

        } catch (e: Exception) {
            e.printStackTrace()
            // 错误时返回
            return AnalysisResult("Error", "Error: ${e.message}")
        }
    }

    // 后面的辅助函数保持原样
    private fun predictSingleWindow(text: String): Int {
        try {
            val tokenIds = tokenize(text)
            val shape = longArrayOf(1, tokenIds.size.toLong())
            val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenIds), shape)
            val attentionMask = LongArray(tokenIds.size) { 1 }
            val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)
            val inputs = mapOf("input_ids" to inputTensor, "attention_mask" to maskTensor)
            val results = session!!.run(inputs)
            val outputTensor = results[0] as OnnxTensor
            val floatBuffer = outputTensor.floatBuffer
            val logits = FloatArray(5)
            floatBuffer.get(logits)
            results.close()
            inputTensor.close()
            maskTensor.close()
            return argmax(logits)
        } catch (e: Exception) {
            e.printStackTrace()
            return 2
        }
    }

    private fun loadVocab(context: Context) {
        context.assets.open("vocab.txt").use { iss ->
            BufferedReader(InputStreamReader(iss)).use { reader ->
                var index = 0
                reader.forEachLine { line ->
                    val token = line.trim()
                    if (token.isNotEmpty()) {
                        vocab[token] = index
                        if (token == "[UNK]") realUnkId = index.toLong()
                        if (token == "[CLS]") clsId = index.toLong()
                        if (token == "[SEP]") sepId = index.toLong()
                    }
                    index++
                }
            }
        }
        println("Vocab Loaded")
    }

    private fun copyAssetToCache(context: Context, fileName: String): String {
        val file = File(context.filesDir, fileName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(fileName).use { inputs ->
            FileOutputStream(file).use { outputs ->
                inputs.copyTo(outputs)
            }
        }
        return file.absolutePath
    }

    private fun argmax(array: FloatArray): Int {
        var maxIdx = 0
        var maxVal = array[0]
        for (i in 1 until array.size) {
            if (array[i] > maxVal) {
                maxVal = array[i]
                maxIdx = i
            }
        }
        return maxIdx
    }

    private fun cleanSocialMediaText(rawText: String): String {
        var text = rawText
        val uiKeywords = listOf("Reply", "Translate", "Follow", "ago", "mins", "hr", "hrs", "Save", "Say something",
            "回复","说点什么...","momo"
        )
        for (kw in uiKeywords) {
            text = text.replace(Regex("(?i)\\b$kw\\b"), "")
        }
        text = text.replace("Reply", "")
        text = text.replace("Translate", "")
        text = text.replace(Regex("\\d{1,2}:\\d{2}"), "")
        text = text.replace(Regex("\\d{1,2}月前"), "")
        text = text.replace(Regex("\\d{1,2}天前"), "")
        text = text.replace(Regex("\\d{1,2}小时前"), "")
        text = text.replace(Regex("\\d{1,2}分钟前"), "")
        text = text.replace(Regex("展开 \\d{1,4} 条回复"), "")
        text = text.replace("Author liked", "")
        text = text.replace(Regex("[A-Za-z ]+, China"), "")
        text = text.replace("\n\n","\n")
        return text
    }

    fun isReady(): Boolean = session != null

    fun close() {
        session?.close()
        env?.close()
    }
}