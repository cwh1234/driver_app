package com.drivertest.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivertest.app.data.local.entity.KnowledgeCardEntity
import com.drivertest.app.data.repository.CardRepository
import com.drivertest.app.domain.model.ReviewStatus
import com.drivertest.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearnUiState(
    val isLoading: Boolean = true,
    val currentCard: KnowledgeCardEntity? = null,
    val queuePosition: Int = 0,
    val queueSize: Int = 0,
    val todayReviewed: Int = 0,
    val isComplete: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    private val reviewQueue = mutableListOf<KnowledgeCardEntity>()
    private var currentIndex = 0

    init {
        loadReviewQueue()
    }

    fun loadReviewQueue() {
        viewModelScope.launch {
            doLoadReviewQueue()
        }
    }

    private suspend fun doLoadReviewQueue() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val today = DateUtils.todayDateString()
            val unreviewed = cardRepository.getUnreviewedCards(today)

            reviewQueue.clear()
            reviewQueue.addAll(unreviewed)
            currentIndex = 0

            if (reviewQueue.isEmpty()) {
                val totalCards = cardRepository.getCardCount()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = totalCards == 0,
                        isComplete = totalCards > 0,
                        currentCard = null,
                        queuePosition = 0,
                        queueSize = 0,
                        todayReviewed = 0
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = false,
                        isComplete = false,
                        currentCard = reviewQueue.first(),
                        queuePosition = 1,
                        queueSize = reviewQueue.size,
                        todayReviewed = 0
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(isLoading = false, error = e.message ?: "加载失败")
            }
        }
    }

    fun markStatus(status: ReviewStatus) {
        val card = _uiState.value.currentCard ?: return

        viewModelScope.launch {
            try {
                cardRepository.recordReview(card.id, status)

                currentIndex++
                val newReviewed = _uiState.value.todayReviewed + 1

                if (currentIndex < reviewQueue.size) {
                    _uiState.update {
                        it.copy(
                            currentCard = reviewQueue[currentIndex],
                            queuePosition = currentIndex + 1,
                            todayReviewed = newReviewed
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentCard = null,
                            queuePosition = reviewQueue.size,
                            todayReviewed = newReviewed,
                            isComplete = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "记录失败")
                }
            }
        }
    }

    fun refreshQueue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                cardRepository.resetTodayReviews()
            } catch (_: Exception) {
                // If deletion fails, still try to reload
            }
            doLoadReviewQueue()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
