package com.drivertest.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_records",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["card_id"]),
        Index(value = ["review_date"]),
        Index(value = ["card_id", "review_date"])
    ]
)
data class ReviewRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "card_id")
    val cardId: Long,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "review_date")
    val reviewDate: String
)
