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

class MultilingualTextClassifier(private val context: Context) {

    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null

    // 词汇表
    private val vocab = HashMap<String, Int>()
    private val unkId = 100L // BERT 默认 UNK ID 通常是 100，具体看 vocab.txt，这里动态获取更安全
    private var realUnkId = 100L
    private var clsId = 101L
    private var sepId = 102L

    // 对应 Python 代码里的 sentiment_map
    // 0: "Very Negative", 1: "Negative", 2: "Neutral", 3: "Positive", 4: "Very Positive"
    private val labels = listOf(
        "非常负面 (Very Negative)",
        "负面 (Negative)",
        "中性 (Neutral)",
        "正面 (Positive)",
        "非常正面 (Very Positive)"
    )

    init {
        try {
            // 1. 加载词汇表
            loadVocab(context)

            // 2. 加载模型
            val modelPath = copyAssetToCache(context, "model.onnx")
            env = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions()
            options.setIntraOpNumThreads(4) // 并行运算线程数，建议设为 2 到 4
            session = env?.createSession(modelPath, options)
            if (session != null) println("Session Loaded")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 核心修复：更完善的分词逻辑
     * 1. 预处理：把中文、标点符号前后加上空格
     * 2. 空格切分
     * 3. WordPiece 子词匹配
     */
    private fun tokenize(text: String): LongArray {
        val tokens = ArrayList<String>()
        tokens.add("[CLS]")

        // --- 步骤 1: 预处理 (处理中文和标点) ---
        val cleanedText = preTokenize(text)

        // --- 步骤 2: 基础切分 (按空格) ---
        val basicParts = cleanedText.trim().split("\\s+".toRegex())

        // --- 步骤 3: WordPiece ---
        for (part in basicParts) {
            if (part.isEmpty()) continue

            // 如果是单个字符且在词表中（比如中文），直接添加
            if (vocab.containsKey(part)) {
                tokens.add(part)
                continue
            }

            // 否则尝试最大正向匹配 (WordPiece)
            var current = part
            var start = 0
            var foundAny = false

            while (start < current.length) {
                var end = current.length
                var subToken: String? = null

                while (end > start) {
                    var sub = current.substring(start, end)
                    if (start > 0) sub = "##$sub" // BERT 的 subword 前缀

                    if (vocab.containsKey(sub)) {
                        subToken = sub
                        break
                    }
                    end--
                }

                if (subToken == null) {
                    // 彻底找不到，标记为 UNK
                    tokens.add("[UNK]")
                    foundAny = true
                    break // 跳过这个词剩下的部分
                } else {
                    tokens.add(subToken)
                    start = end
                    foundAny = true
                }
            }
            if (!foundAny) tokens.add("[UNK]")
        }

        tokens.add("[SEP]")

        // 转换为 ID
        return tokens.map { vocab[it]?.toLong() ?: realUnkId }.toLongArray()
    }

    /**
     * 关键函数：处理中文和标点
     * 这里的逻辑模仿了 HuggingFace 的 BasicTokenizer
     */
    private fun preTokenize(text: String): String { //For Chinese
        val sb = StringBuilder()
        for (i in text.indices) {
            val c = text[i]
            if (isChineseChar(c) || isPunctuation(c)) {
                // 如果是中文或标点，前后加空格，确保能被 split 切开
                sb.append(" ").append(c).append(" ")
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    // 判断是否是中日韩字符
    private fun isChineseChar(c: Char): Boolean {
        // CJK Unified Ideographs
        return (c.code in 0x4E00..0x9FFF) ||
                (c.code in 0x3400..0x4DBF) ||
                (c.code in 0x20000..0x2A6DF) ||
                (c.code in 0x2A700..0x2B73F) ||
                (c.code in 0x2B740..0x2B81F) ||
                (c.code in 0x2B820..0x2CEAF) ||
                (c.code in 0xF900..0xFAFF) ||
                (c.code in 0x2F800..0x2FA1F)
    }

    // 简单的标点判断
    private fun isPunctuation(c: Char): Boolean {
        if (c.isLetterOrDigit() || c.isWhitespace()) return false
        // 包含常见的 ASCII 标点和中文标点
        return true
    }

    fun analyze(Rawtext: String): String {
        if (session == null || vocab.isEmpty()) return "模型加载中..."
        val text = cleanSocialMediaText(Rawtext)
        println("==============Cleaned Text==============\n $text \n\n\n")
        try {
            val tokenIds = tokenize(text)

            var sentences = text.split(Regex("[!?;。\n！？；]")) //Split different comments

            sentences = sentences.filter{ it.length > 2 && !it.matches(Regex("^[0-9]+$")) } //filter punctuations and numbers

            //Sliding Window
            if (sentences.size < 3) {
                val idx = predictSingleWindow(text)
                return labels[idx]
            }

            var count = intArrayOf(0,0,0,0,0)
            var minIndex = 10 //initialize with 100

            for (i in 0 until sentences.size - 2) {
                val windowText = "${sentences[i]}, ${sentences[i+1]}, ${sentences[i+2]}"
                val index = predictSingleWindow(windowText)
                if (index < minIndex) minIndex = index
                if (index == 0) println("Very Negative: $windowText")
                else if (index == 1) println("Negative: $windowText")
                count[index] ++
            }
            println("minIndex: $minIndex, count[0]= ${count[0]}, count[1]= ${count[1]}, count[2]= ${count[2]}, count[3]= ${count[3]}, count[4]= ${count[4]}")
            if (minIndex == 100) return labels[2] //nothing's there

            if (minIndex == 0) return labels[0] //very negetive

            val totalCount = 1.5*count[1] + count[2] + 0.8*count[3] + 1.2*count[4]

            if (minIndex == 1) { //there is negative but we can discuss
                //40% 30% 10% 20%
                val weighedsum = ((1.5*1*count[1] + 2*count[2] + 0.8*3*count[3] + 1.2*4*count[4])/totalCount)
                println("weighedsum = $weighedsum")
                return labels[weighedsum.roundToInt()]
            }

            return labels[(2*count[2] + 3*count[3] + 4*count[4]) / (count[2] + count[3] + count[4])]


            /*// Android OnnxRuntime 要求 shape [batch, seq_len]
            val shape = longArrayOf(1, tokenIds.size.toLong())
            val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenIds), shape)

            // 构造 attention mask (全 1)
            val attentionMask = LongArray(tokenIds.size) { 1 }
            val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape)

            val inputs = mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor
            )

            val results = session!!.run(inputs)
            val outputTensor = results[0] as OnnxTensor // 这里的 output 实际上是 logits
            val floatBuffer = outputTensor.floatBuffer

            val logits = FloatArray(5)
            floatBuffer.get(logits)

// --- 新增调试代码 ---
            val labels = listOf("非常负面", "负面", "中性", "正面", "非常正面")
            val sb = StringBuilder()
            for (i in logits.indices) {
                sb.append("${labels[i]}: ${logits[i]}\n")
            }
            println("=== 模型详细打分 ===\n$sb")
// -------------------

            val maxIndex = argmax(logits)
            return labels[maxIndex]

            // Softmax (虽然只求最大值不需要 softmax，但为了严谨或者你想看概率可以加上)
            // 这里直接取 argmax 即可
            /*val maxIndex = argmax(logits)

            return labels[maxIndex] //+ " (Score: ${logits[maxIndex]})" // 如果你想看分数可以把后面解开*/
        */
        } catch (e: Exception) {
            e.printStackTrace()
            return "错误: ${e.message}"
        }
    }

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

            // 释放资源，防止内存泄漏
            results.close()
            inputTensor.close()
            maskTensor.close()
            // outputTensor 不需要单独 close，关闭 results 即可

            return argmax(logits)
        } catch (e: Exception) {
            e.printStackTrace()
            return 2 // 出错默认中性
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
                        // 自动校准特殊 token 的 ID
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

        // 优化：如果文件存在且大小大于0，直接返回路径，不再拷贝
        // 严谨的做法是校验 MD5，但为了速度，校验文件存在通常够用了
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        // 只有文件不存在时才执行拷贝
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

        // 1. 去除常见的 UI 关键词 (不区分大小写)
        val uiKeywords = listOf("Reply", "Translate", "Follow", "ago", "mins", "hr", "hrs", "Save", "Say something",
            "回复","说点什么...","momo"
        )
        for (kw in uiKeywords) {
            text = text.replace(Regex("(?i)\\b$kw\\b"), "") // \b 匹配单词边界
        }
        text = text.replace("Reply", "")
        text = text.replace("Translate", "")
        // 2. 去除时间戳 (如 18:20)
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