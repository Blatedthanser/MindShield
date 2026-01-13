package com.example.mindshield.data.repository

import com.example.mindshield.model.ChartDataPoint
import com.example.mindshield.model.EmotionalEvent

val MOCK_EVENTS = listOf(
    EmotionalEvent("1", "22:15", "Weibo", listOf("杠精", "甚至"), 112, "明明是你自己不懂逻辑，甚至还在这里指点江山..."),
    EmotionalEvent("2", "19:40", "X (Twitter)", listOf("stupid", "waste"), 98, "This is the most stupid take I have ever seen on this app..."),
    EmotionalEvent("3", "14:20", "Work Chat", listOf("asap", "urgent"), 105, "Need this done ASAP. Why is it taking so long?")
)

val HOURLY_STRESS_DATA = listOf(
    ChartDataPoint("8am", 2), ChartDataPoint("12pm", 5), ChartDataPoint("4pm", 3),
    ChartDataPoint("8pm", 6), ChartDataPoint("10pm", 8), ChartDataPoint("12am", 4)
)

val APP_RANKING_DATA = listOf(
    ChartDataPoint("Weibo", 45), ChartDataPoint("TikTok", 30),
    ChartDataPoint("Game", 15), ChartDataPoint("Work", 10)
)