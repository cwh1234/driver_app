package com.drivertest.app.data.repository

import com.drivertest.app.data.local.dao.KnowledgeCardDao
import com.drivertest.app.data.local.dao.ReviewRecordDao
import com.drivertest.app.domain.model.CardWithStats
import com.drivertest.app.domain.model.DailyReviewCount
import com.drivertest.app.domain.model.ReviewStatus
import com.drivertest.app.domain.model.StatsSummary
import com.drivertest.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val cardDao: KnowledgeCardDao,
    private val reviewDao: ReviewRecordDao
) {
    suspend fun getStatsSummary(): StatsSummary {
        val totalCards = cardDao.getCardCount()
        val today = DateUtils.todayDateString()
        val todayReviewed = reviewDao.getReviewedCardCountForDate(today)
        val totalReviews = reviewDao.getTotalReviewCount()

        // Count mastered cards from latest status
        val latestStatuses = reviewDao.getLatestStatusForAllCards().first()
        val masteredCount = latestStatuses.count { it.status == ReviewStatus.MASTERED.dbValue }
        val streak = calculateStreak()

        return StatsSummary(
            totalCards = totalCards,
            todayReviewed = todayReviewed,
            masteredCount = masteredCount,
            masteryRate = if (totalCards > 0) masteredCount.toFloat() / totalCards else 0f,
            currentStreak = streak,
            totalReviews = totalReviews
        )
    }

    fun getDailyReviewCounts(days: Int = 7): Flow<List<DailyReviewCount>> {
        val sinceDate = DateUtils.daysAgoDateString(days - 1)
        return reviewDao.getDailyReviewCounts(sinceDate)
    }

    suspend fun getCardsWithStats(): List<CardWithStats> {
        val cards = cardDao.getAllCards().first()
        val latestStatuses = reviewDao.getLatestStatusForAllCards().first()

        return cards.map { card ->
            val latestStatus = latestStatuses
                .firstOrNull { it.cardId == card.id }
                ?.let { ReviewStatus.fromDbValue(it.status) }
            val reviewCount = reviewDao.getReviewCountForCard(card.id)

            CardWithStats(
                card = card,
                reviewCount = reviewCount,
                latestStatus = latestStatus
            )
        }
    }

    private suspend fun calculateStreak(): Int {
        val latestDate = reviewDao.getLatestReviewDate() ?: return 0
        val today = DateUtils.todayDateString()

        // No review today or yesterday → streak broken
        if (latestDate != today && latestDate != DateUtils.yesterdayDateString()) {
            return 0
        }

        var streak = 0
        var daysBack = 0

        while (true) {
            val checkDate = DateUtils.daysAgoDateString(daysBack)
            val count = reviewDao.getReviewedCardCountForDate(checkDate)
            if (count > 0) {
                streak++
                daysBack++
            } else {
                break
            }
        }

        return streak
    }
}
