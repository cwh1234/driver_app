package com.drivertest.app.util

import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import com.drivertest.app.domain.model.CardSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AiGeneratedCard(
    val title: String,
    val content: String
)

object JsonParser {

    private val gson = Gson()

    fun parseAiGeneratedCards(rawResponse: String): Result<List<KnowledgeCardEntity>> {
        return try {
            // Step 1: Extract JSON from possible markdown code fences
            val jsonString = extractJson(rawResponse)

            // Step 2: Parse JSON array
            val type = object : TypeToken<List<AiGeneratedCard>>() {}.type
            val aiCards: List<AiGeneratedCard> = gson.fromJson(jsonString, type)

            // Step 3: Validate
            if (aiCards.isEmpty()) {
                return Result.failure(Exception("AI未生成任何卡片，请尝试更具体的主题"))
            }

            // Step 4: Map to entities
            val now = System.currentTimeMillis()
            val entities = aiCards.map { card ->
                KnowledgeCardEntity(
                    title = card.title.trim(),
                    content = card.content.trim(),
                    source = CardSource.AI_GENERATED.dbValue,
                    createdAt = now,
                    updatedAt = now
                )
            }

            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(Exception("AI返回格式解析失败: ${e.message}"))
        }
    }

    private fun extractJson(text: String): String {
        var result = text.trim()

        // Remove markdown code fences: ```json ... ``` or ``` ... ```
        val codeFenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.DOT_MATCHES_ALL)
        val match = codeFenceRegex.find(result)
        if (match != null) {
            result = match.groupValues[1].trim()
        }

        // Find first '[' and last ']' to extract the JSON array
        val startIndex = result.indexOf('[')
        val endIndex = result.lastIndexOf(']')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            result = result.substring(startIndex, endIndex + 1)
        }

        return result
    }
}
