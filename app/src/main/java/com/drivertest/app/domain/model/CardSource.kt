package com.drivertest.app.domain.model

enum class CardSource(val label: String, val dbValue: String) {
    TEXT_INPUT("文本输入", "TEXT_INPUT"),
    PHOTO_OCR("拍照识别", "PHOTO_OCR"),
    AI_GENERATED("AI生成", "AI_GENERATED"),
    IMAGE_IMPORT("图片导入", "IMAGE_IMPORT");

    companion object {
        fun fromDbValue(value: String): CardSource =
            entries.firstOrNull { it.dbValue == value } ?: TEXT_INPUT
    }
}
