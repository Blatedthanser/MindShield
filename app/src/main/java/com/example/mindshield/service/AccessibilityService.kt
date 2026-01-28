package com.example.mindshield.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.mindshield.domain.analysis.MultilingualTextClassifier
import android.content.pm.PackageManager

class MindShieldAccessibilityService : AccessibilityService() {

    private lateinit var classifier: MultilingualTextClassifier

    companion object {
        const val TAG = "Accessibility"

        private var instance: MindShieldAccessibilityService? = null

        fun startDiagnosisFromActivity() {
            instance?.startTextDiagnosis() ?: Log.e("MindShield", "服务未开启，无法启动任务")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        classifier = MultilingualTextClassifier(applicationContext)

        instance = this
        Log.d("MindShield", "Accessibility instance has been bound to: $this")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Nothing's here
    }

    fun startTextDiagnosis() {
        val rootNode = rootInActiveWindow ?: return
        // 获取 App 名称
        val packageName = rootNode.packageName?.toString() ?: "Unknown"
        val appName = getAppNameFromPackage(packageName)
        val allTexts = collectAllTexts(rootNode)
        val mergedText = buildMergedText(allTexts)
        val analysisResult = classifier.analyze(mergedText)
        // 截取一小段文字用于显示
        val snippet = analysisResult.triggerText
        val resultLabel = analysisResult.label
        println("==================分析结果=================")
        println("Label: $resultLabel")
        println("Trigger Text: $snippet")
        // 传入 appName 和 具体的 snippet
        MindShieldService.judgeOnResponse(resultLabel, appName, snippet)
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            // 如果获取失败，做个简单映射
            when {
                packageName.contains("twitter") -> "X (Twitter)"
                packageName.contains("weibo") -> "Weibo"
                packageName.contains("tiktok") -> "TikTok"
                packageName.contains("xingin") -> "Rednote"
                else -> "Unknown App"
            }
        }
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

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null // 断开时置空
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.e(TAG, "服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}