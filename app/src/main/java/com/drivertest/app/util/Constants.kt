package com.drivertest.app.util

object Constants {
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1/"
    const val DEEPSEEK_MODEL = "deepseek-chat"

    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

    val AI_SYSTEM_PROMPT = """
你是一个知识卡片生成助手。你的任务是根据用户提供的主题，生成准确、简洁的知识卡片。

要求：
1. 生成3-5个知识卡片（如果主题很窄，可以只生成1-2个）
2. 每个卡片包含：标题(title)和详细内容(content)
3. 内容必须准确无误，简洁明了，适合记忆
4. 严格以JSON数组格式返回，不要包含markdown标记或额外解释
5. JSON格式：[{"title":"卡片标题","content":"卡片详细内容"},...]
6. 标题精炼（不超过20字），内容详实但不过长（不超过200字）
""".trimIndent()

    const val AI_USER_PROMPT_PREFIX = "请为以下主题生成知识卡片："
}
