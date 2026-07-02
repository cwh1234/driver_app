package com.drivertest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drivertest.app.data.local.entity.ReviewRecordEntity
import com.drivertest.app.domain.model.DailyReviewCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(record: ReviewRecordEntity)

    @Query("SELECT * FROM review_records WHERE card_id = :cardId ORDER BY reviewed_at DESC")
    fun getReviewsForCard(cardId: Long): Flow<List<ReviewRecordEntity>>

    @Query("SELECT COUNT(*) FROM review_records WHERE card_id = :cardId")
    suspend fun getReviewCountForCard(cardId: Long): Int

    @Query("""
        SELECT review_date AS date, COUNT(DISTINCT card_id) AS count
        FROM review_records
        WHERE review_date >= :sinceDate
        GROUP BY review_date
        ORDER BY review_date DESC
    """)
    fun getDailyReviewCounts(sinceDate: String): Flow<List<DailyReviewCount>>

    @Query("SELECT COUNT(DISTINCT card_id) FROM review_records WHERE review_date = :date")
    suspend fun getReviewedCardCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM review_records")
    suspend fun getTotalReviewCount(): Int

    @Query("""
        SELECT DISTINCT review_date FROM review_records
        ORDER BY review_date DESC
        LIMIT 1
    """)
    suspend fun getLatestReviewDate(): String?

    @Query("""
        SELECT r1.status FROM review_records r1
        WHERE r1.card_id = :cardId
        ORDER BY r1.reviewed_at DESC
        LIMIT 1
    """)
    suspend fun getLatestStatusForCard(cardId: Long): String?

    @Query("""
        SELECT r1.card_id AS cardId, r1.status AS status
        FROM review_records r1
        INNER JOIN (
            SELECT card_id, MAX(reviewed_at) AS max_reviewed
            FROM review_records
            GROUP BY card_id
        ) r2 ON r1.card_id = r2.card_id AND r1.reviewed_at = r2.max_reviewed
    """)
    fun getLatestStatusForAllCards(): Flow<List<CardLatestStatus>>

    @Query("DELETE FROM review_records WHERE review_date = :date")
    suspend fun deleteReviewsForDate(date: String)
}

data class CardLatestStatus(
    val cardId: Long,
    val status: String
)
