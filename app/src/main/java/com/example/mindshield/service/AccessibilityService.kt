package com.example.mindshield.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.mindshield.domain.analysis.MultilingualTextClassifier

class MindShieldAccessibilityService : AccessibilityService() {

    private lateinit var classifier: MultilingualTextClassifier

    companion object {
        const val TAG = "Accessibility"

        private var instance: MindShieldAccessibilityService? = null

        // 供外部调用的公开方法
        fun startDiagnosisFromActivity() {
            instance?.startTextDiagnosis() ?: Log.e("MindShield", "服务未开启，无法启动任务")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        classifier = MultilingualTextClassifier(applicationContext)

        instance = this
        Log.d("MindShield", "服务已连接，实例已注册")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        val rootNode = rootInActiveWindow ?: return
//        val allTexts = collectAllTexts(rootNode)
//        val mergedText = buildMergedText(allTexts)
//        println("==================读取文字=================")
//        println(mergedText)
//        val result = classifier.analyze(mergedText)
//        println("==================分析结果=================")
//        println(result)
    }

    fun startTextDiagnosis() {
        val rootNode = rootInActiveWindow ?: return
        val allTexts = collectAllTexts(rootNode)
        val mergedText = buildMergedText(allTexts)
        println("==================读取文字=================")
        println(mergedText)
        val result = classifier.analyze(mergedText)
        println("==================分析结果=================")
        println(result)
        MindShieldService.judgeOnResponse(result)
    }

    private fun collectAllTexts(node: AccessibilityNodeInfo?): List<String> {
        val result = mutableListOf<String>()
        if (node == null) return result

        // 如果节点有文本，添加到结果列表
        node.text?.let { text ->
            if (text.isNotEmpty()) {
                result.add(text.toString())
            }
        }

        // 递归遍历所有子节点
        for (i in 0 until node.childCount) {
            result.addAll(collectAllTexts(node.getChild(i)))
        }

        return result
    }

    private fun buildMergedText(
        texts: List<String>,
        maxLength: Int = 512
    ): String {

        val sb = StringBuilder()

        for (text in texts) {
            val trimmed = text.trim()

            // 过滤：全是数字的字符串
            if (trimmed.isEmpty() || trimmed.all { it.isDigit() }) {
                continue
            }

            // 如果加上这一行会超长，就截断并结束
            if (sb.length + trimmed.length + 1 > maxLength) {
                val remain = maxLength - sb.length
                if (remain > 0) {
                    sb.append(trimmed.take(remain))
                }
                break
            }

            sb.append(trimmed)
            sb.append('\n')
        }

        return sb.toString()
    }


    override fun onInterrupt() {
        Log.e(TAG, "服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}