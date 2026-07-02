package com.drivertest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeCardDao {

    @Query("SELECT * FROM knowledge_cards ORDER BY created_at DESC")
    fun getAllCards(): Flow<List<KnowledgeCardEntity>>

    @Query("SELECT * FROM knowledge_cards WHERE id = :id")
    suspend fun getCardById(id: Long): KnowledgeCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: KnowledgeCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<KnowledgeCardEntity>): List<Long>

    @Update
    suspend fun updateCard(card: KnowledgeCardEntity)

    @Delete
    suspend fun deleteCard(card: KnowledgeCardEntity)

    @Query("SELECT COUNT(*) FROM knowledge_cards")
    suspend fun getCardCount(): Int

    @Query("""
        SELECT * FROM knowledge_cards
        WHERE id NOT IN (
            SELECT DISTINCT card_id FROM review_records WHERE review_date = :today
        )
        ORDER BY created_at ASC
    """)
    suspend fun getUnreviewedCards(today: String): List<KnowledgeCardEntity>

    @Query("""
        SELECT * FROM knowledge_cards
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchCards(query: String): Flow<List<KnowledgeCardEntity>>
}
