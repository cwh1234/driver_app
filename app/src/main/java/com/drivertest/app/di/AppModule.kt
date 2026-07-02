package com.drivertest.app.di

import android.content.Context
import com.drivertest.app.ocr.OcrProcessor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOcrProcessor(@ApplicationContext context: Context): OcrProcessor =
        OcrProcessor(context)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
