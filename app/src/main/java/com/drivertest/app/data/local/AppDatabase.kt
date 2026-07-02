package com.drivertest.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.drivertest.app.data.local.dao.KnowledgeCardDao
import com.drivertest.app.data.local.dao.ReviewRecordDao
import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import com.drivertest.app.data.local.entity.ReviewRecordEntity

@Database(
    entities = [KnowledgeCardEntity::class, ReviewRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun knowledgeCardDao(): KnowledgeCardDao
    abstract fun reviewRecordDao(): ReviewRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE knowledge_cards ADD COLUMN image_path TEXT DEFAULT NULL"
                )
            }
        }
    }
}
