package com.drivertest.app.domain.model

enum class ReviewStatus(val label: String, val dbValue: String) {
    NOT_FAMILIAR("不熟悉", "NOT_FAMILIAR"),
    UNCLEAR("模糊", "UNCLEAR"),
    MASTERED("掌握", "MASTERED");

    companion object {
        fun fromDbValue(value: String): ReviewStatus =
            entries.firstOrNull { it.dbValue == value } ?: NOT_FAMILIAR
    }
}
