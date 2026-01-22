package com.example.mindshield.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindshield.model.InterventionEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionDao {
    // 插入一条数据，如果 ID 冲突则替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: InterventionEvent)

    // 查询所有数据，按时间倒序排列 (最新的在最上面)
    // 返回 Flow，这样数据库一更新，UI 就会自动收到通知
    @Query("SELECT * FROM intervention_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<InterventionEvent>>

    @Query("DELETE FROM intervention_events")
    suspend fun deleteAllEvents()
}