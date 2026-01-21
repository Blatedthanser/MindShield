package com.example.mindshield.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mindshield.model.InterventionEvent

// 定义数据库包含哪些表 (entities) 和版本号
@Database(entities = [InterventionEvent::class], version = 1, exportSchema = false)
abstract class MindShieldDatabase : RoomDatabase() {

    // 提供 DAO 的获取方法
    abstract fun interventionDao(): InterventionDao

    companion object {
        @Volatile
        private var INSTANCE: MindShieldDatabase? = null

        // 单例模式获取数据库实例
        fun getDatabase(context: Context): MindShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindShieldDatabase::class.java,
                    "mindshield_database" // 手机里的数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}