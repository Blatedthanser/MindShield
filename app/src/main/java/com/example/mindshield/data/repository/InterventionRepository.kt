package com.example.mindshield.data.repository

import android.content.Context
import com.example.mindshield.data.database.MindShieldDatabase
import com.example.mindshield.model.InterventionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Calendar

// 图表用的数据模型
data class ChartData(val name: String, val value: Int)

object InterventionRepository {

    private var database: MindShieldDatabase? = null

    // 初始化方法：Service 启动时需要调用这个，传入 Context 才能创建数据库
    fun init(context: Context) {
        if (database == null) {
            database = MindShieldDatabase.getDatabase(context)
        }
    }

    // 数据源：不再是 MutableStateFlow，而是直接从数据库 DAO 获取 Flow
    val events: Flow<List<InterventionEvent>>
        get() = database?.interventionDao()?.getAllEvents() ?: emptyFlow()

    // 添加事件：变成 suspend 挂起函数，写入数据库
    suspend fun addEvent(event: InterventionEvent) {
        database?.interventionDao()?.insertEvent(event)
    }

    fun getHourlyStressData(eventsList: List<InterventionEvent>): List<ChartData> {
        val distribution = IntArray(24) { 0 }
        val calendar = Calendar.getInstance()

        eventsList.forEach { event ->
            calendar.timeInMillis = event.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            distribution[hour]++
        }

        // 显示关键时间点
        val hoursToShow = listOf(8, 12, 16, 20, 22, 0)
        return hoursToShow.map { hour ->
            val label = if (hour == 0) "12am" else if (hour == 12) "12pm" else if (hour > 12) "${hour-12}pm" else "${hour}am"
            ChartData(label, distribution[hour])
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
                ChartData(name, percentage)
            }
            .sortedByDescending { it.value }
            .take(4)
    }
}