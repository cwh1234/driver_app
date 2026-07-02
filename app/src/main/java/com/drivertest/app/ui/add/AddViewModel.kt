package com.drivertest.app.ui.add

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import com.drivertest.app.data.remote.DeepSeekApiService
import com.drivertest.app.data.remote.dto.ChatMessage
import com.drivertest.app.data.remote.dto.DeepSeekRequest
import com.drivertest.app.data.repository.CardRepository
import com.drivertest.app.ocr.OcrProcessor
import com.drivertest.app.util.AiGeneratedCard
import com.drivertest.app.util.Constants
import com.drivertest.app.util.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AddUiState(
    // Tab
    val selectedTab: Int = 0,

    // Text Input
    val textTitle: String = "",
    val textContent: String = "",

    // Photo OCR
    val capturedBitmap: Bitmap? = null,
    val ocrTitle: String = "",
    val ocrContent: String = "",
    val isOcrProcessing: Boolean = false,
    val ocrError: String? = null,

    // Image Import
    val imageBitmap: Bitmap? = null,
    val imagePath: String? = null,

    // AI Search
    val searchQuery: String = "",
    val isAiSearching: Boolean = false,
    val generatedCards: List<KnowledgeCardEntity> = emptyList(),
    val selectedCardIndices: Set<Int> = emptySet(),
    val aiError: String? = null,

    // Shared
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class AddViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val ocrProcessor: OcrProcessor,
    private val deepSeekApiService: DeepSeekApiService,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    // === Tab Management ===
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // === Text Input ===
    fun updateTitle(text: String) {
        _uiState.update { it.copy(textTitle = text, saveSuccess = false) }
    }

    fun updateContent(text: String) {
        _uiState.update { it.copy(textContent = text, saveSuccess = false) }
    }

    fun saveTextCard() {
        val state = _uiState.value
        if (state.textTitle.isBlank() || state.textContent.isBlank()) {
            _uiState.update { it.copy(saveError = "标题和内容不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                cardRepository.addTextCard(state.textTitle.trim(), state.textContent.trim())
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        textTitle = "",
                        textContent = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "保存失败: ${e.message}") }
            }
        }
    }

    // === Photo OCR ===
    fun onPhotoTaken(bitmap: Bitmap) {
        _uiState.update { it.copy(capturedBitmap = bitmap, ocrError = null) }
        processOcr(bitmap)
    }

    private fun processOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrProcessing = true, ocrError = null) }
            ocrProcessor.recognizeText(bitmap)
                .onSuccess { text ->
                    val content = text.trim()
                    _uiState.update {
                        it.copy(
                            isOcrProcessing = false,
                            ocrTitle = "",
                            ocrContent = content.take(2000)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isOcrProcessing = false,
                            ocrError = "文字识别失败: ${e.message}"
                        )
                    }
                }
        }
    }

    fun updateOcrTitle(text: String) {
        _uiState.update { it.copy(ocrTitle = text) }
    }

    fun updateOcrContent(text: String) {
        _uiState.update { it.copy(ocrContent = text) }
    }

    fun retakePhoto() {
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                ocrTitle = "",
                ocrContent = "",
                ocrError = null
            )
        }
    }

    fun saveOcrCard() {
        val state = _uiState.value
        if (state.ocrContent.isBlank()) {
            _uiState.update { it.copy(saveError = "内容不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                // Use user-entered title, or first line of content as title, or "OCR识别"
                val title = state.ocrTitle.ifBlank {
                    state.ocrContent.lines().firstOrNull()?.take(50)?.ifBlank { "OCR识别" } ?: "OCR识别"
                }
                cardRepository.addOcrCard(title.trim(), state.ocrContent.trim())
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        capturedBitmap = null,
                        ocrTitle = "",
                        ocrContent = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "保存失败: ${e.message}") }
            }
        }
    }

    // === AI Search ===
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, aiError = null) }
    }

    fun performAiSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(aiError = "请输入搜索主题") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiSearching = true, aiError = null, generatedCards = emptyList(), selectedCardIndices = emptySet()) }
            try {
                val request = DeepSeekRequest(
                    model = Constants.DEEPSEEK_MODEL,
                    messages = listOf(
                        ChatMessage(role = "system", content = Constants.AI_SYSTEM_PROMPT),
                        ChatMessage(role = "user", content = "${Constants.AI_USER_PROMPT_PREFIX}$query")
                    )
                )

                val response = deepSeekApiService.chatCompletion(request)
                val rawContent = response.choices.firstOrNull()?.message?.content
                    ?: throw Exception("AI未返回有效响应")

                JsonParser.parseAiGeneratedCards(rawContent)
                    .onSuccess { cards ->
                        _uiState.update {
                            it.copy(
                                isAiSearching = false,
                                generatedCards = cards
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(isAiSearching = false, aiError = e.message)
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAiSearching = false,
                        aiError = "AI请求失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleCardSelection(index: Int) {
        _uiState.update { state ->
            val newSet = state.selectedCardIndices.toMutableSet()
            if (newSet.contains(index)) newSet.remove(index) else newSet.add(index)
            state.copy(selectedCardIndices = newSet)
        }
    }

    fun saveSelectedCards() {
        val state = _uiState.value
        val selectedCards = state.selectedCardIndices.mapNotNull { index ->
            state.generatedCards.getOrNull(index)
        }

        if (selectedCards.isEmpty()) {
            _uiState.update { it.copy(saveError = "请至少选择一张卡片") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                cardRepository.addAiCards(selectedCards)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        searchQuery = "",
                        generatedCards = emptyList(),
                        selectedCardIndices = emptySet()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "保存失败: ${e.message}") }
            }
        }
    }

    // === Image Import ===
    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                imageBitmap = bitmap,
                imagePath = null,
                saveSuccess = false
            )
        }
    }

    fun clearImage() {
        // Delete the saved image file if exists
        val path = _uiState.value.imagePath
        if (path != null) {
            try {
                java.io.File(path).delete()
            } catch (_: Exception) {}
        }
        _uiState.update {
            it.copy(
                imageBitmap = null,
                imagePath = null,
                saveSuccess = false
            )
        }
    }

    fun saveImageCard() {
        val state = _uiState.value
        if (state.imageBitmap == null) {
            _uiState.update { it.copy(saveError = "请先选择图片") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                // Save bitmap to internal storage
                val savedPath = saveBitmapToInternal(state.imageBitmap!!)
                // Auto-generate title from timestamp; image IS the content
                val title = "图片卡片 ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                cardRepository.addImageCard(
                    title,
                    "图片知识卡片",
                    savedPath
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        imageBitmap = null,
                        imagePath = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "保存失败: ${e.message}") }
            }
        }
    }

    private fun saveBitmapToInternal(bitmap: Bitmap): String {
        val dir = File(appContext.filesDir, "card_images")
        if (!dir.exists()) dir.mkdirs()
        val fileName = "card_img_${System.currentTimeMillis()}.jpg"
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(saveError = null, saveSuccess = false, aiError = null, ocrError = null)
        }
    }
}
