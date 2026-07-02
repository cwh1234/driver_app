package com.drivertest.app.di

import android.content.Context
import androidx.room.Room
import com.drivertest.app.data.local.AppDatabase
import com.drivertest.app.data.local.dao.KnowledgeCardDao
import com.drivertest.app.data.local.dao.ReviewRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "drivertest.db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideKnowledgeCardDao(db: AppDatabase): KnowledgeCardDao =
        db.knowledgeCardDao()

    @Provides
    fun provideReviewRecordDao(db: AppDatabase): ReviewRecordDao =
        db.reviewRecordDao()
}
