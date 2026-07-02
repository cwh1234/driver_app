package com.drivertest.app.data.repository

import com.drivertest.app.data.local.dao.KnowledgeCardDao
import com.drivertest.app.data.local.dao.ReviewRecordDao
import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import com.drivertest.app.data.local.entity.ReviewRecordEntity
import com.drivertest.app.domain.model.CardSource
import com.drivertest.app.domain.model.ReviewStatus
import com.drivertest.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: KnowledgeCardDao,
    private val reviewDao: ReviewRecordDao
) {
    fun getAllCards(): Flow<List<KnowledgeCardEntity>> = cardDao.getAllCards()

    suspend fun getUnreviewedCards(today: String): List<KnowledgeCardEntity> =
        cardDao.getUnreviewedCards(today)

    suspend fun addTextCard(title: String, content: String): Long {
        val now = System.currentTimeMillis()
        val card = KnowledgeCardEntity(
            title = title,
            content = content,
            source = CardSource.TEXT_INPUT.dbValue,
            createdAt = now,
            updatedAt = now
        )
        return cardDao.insertCard(card)
    }

    suspend fun addOcrCard(title: String, content: String): Long {
        val now = System.currentTimeMillis()
        val card = KnowledgeCardEntity(
            title = title,
            content = content,
            source = CardSource.PHOTO_OCR.dbValue,
            createdAt = now,
            updatedAt = now
        )
        return cardDao.insertCard(card)
    }

    suspend fun addAiCards(cards: List<KnowledgeCardEntity>): List<Long> =
        cardDao.insertCards(cards)

    suspend fun addImageCard(title: String, content: String, imagePath: String): Long {
        val now = System.currentTimeMillis()
        val card = KnowledgeCardEntity(
            title = title,
            content = content,
            source = CardSource.IMAGE_IMPORT.dbValue,
            imagePath = imagePath,
            createdAt = now,
            updatedAt = now
        )
        return cardDao.insertCard(card)
    }

    suspend fun recordReview(cardId: Long, status: ReviewStatus) {
        val record = ReviewRecordEntity(
            cardId = cardId,
            status = status.dbValue,
            reviewedAt = System.currentTimeMillis(),
            reviewDate = DateUtils.todayDateString()
        )
        reviewDao.insertReview(record)
    }

    suspend fun getCardCount(): Int = cardDao.getCardCount()

    fun searchCards(query: String): Flow<List<KnowledgeCardEntity>> =
        cardDao.searchCards(query)

    suspend fun deleteCard(card: KnowledgeCardEntity) = cardDao.deleteCard(card)

    suspend fun resetTodayReviews() {
        reviewDao.deleteReviewsForDate(DateUtils.todayDateString())
    }
}
