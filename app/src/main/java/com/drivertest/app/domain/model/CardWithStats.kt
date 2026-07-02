package com.drivertest.app.domain.model

import com.drivertest.app.data.local.entity.KnowledgeCardEntity

data class CardWithStats(
    val card: KnowledgeCardEntity,
    val reviewCount: Int,
    val latestStatus: ReviewStatus?
)
