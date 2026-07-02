package com.drivertest.app.domain.model

data class StatsSummary(
    val totalCards: Int = 0,
    val todayReviewed: Int = 0,
    val masteredCount: Int = 0,
    val masteryRate: Float = 0f,
    val currentStreak: Int = 0,
    val totalReviews: Int = 0
)
