package com.example.mindshield.data.repository

import android.content.Context
import com.example.mindshield.data.database.MindShieldDatabase
import com.example.mindshield.model.InterventionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Calendar

// 图表用的数据模型
data class ChartData(val name: String, val count: Int, val percentage: Int)

object InterventionRepository {

    private var database: MindShieldDatabase? = null

    // 初始化方法：Service 启动时需要调用这个，传入 Context 才能创建数据库
    fun init(context: Context) {
        if (database == null) {
            database = MindShieldDatabase.getDatabase(context)
        }
    }

    // 数据源：从数据库 DAO 获取 Flow
    val events: Flow<List<InterventionEvent>>
        get() = database?.interventionDao()?.getAllEvents() ?: emptyFlow()

    // 添加事件
    suspend fun addEvent(event: InterventionEvent) {
        database?.interventionDao()?.insertEvent(event)
    }

    suspend fun clearAllData() {
        database?.interventionDao()?.deleteAllEvents()
    }

    fun getHourlyStressData(
        events: List<InterventionEvent>
    ): List<ChartData> {

        val labels = listOf("8am", "12pm", "4pm", "8pm", "10pm", "12am")
        val counts = IntArray(labels.size)
        val calendar = Calendar.getInstance()
        events.forEach { event ->
            calendar.timeInMillis = event.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            val index = when (hour) {
                in 8 until 12 -> 0
                in 12 until 16 -> 1
                in 16 until 20 -> 2
                in 20 until 22 -> 3
                in 22 until 24 -> 4
                else -> 5
            }
            counts[index]++
        }
        return labels.mapIndexed { i, label ->
            ChartData(label, counts[i], 0)
        }
    }


    // 计算“most irritable apps”
    fun getAppRankingData(eventsList: List<InterventionEvent>): List<ChartData> {
        if (eventsList.isEmpty()) return emptyList()

        val total = eventsList.size.toFloat()
        return eventsList.groupBy { it.appName }
            .map { (name, list) ->
                val count = list.size
                val percentage = ((count / total) * 100).toInt()
                ChartData(name, count, percentage)
            }
            .sortedByDescending { it.count }
            .take(4)
    }
}