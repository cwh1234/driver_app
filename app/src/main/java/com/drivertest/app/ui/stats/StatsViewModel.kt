package com.drivertest.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivertest.app.data.repository.CardRepository
import com.drivertest.app.data.repository.StatsRepository
import com.drivertest.app.domain.model.CardWithStats
import com.drivertest.app.domain.model.DailyReviewCount
import com.drivertest.app.domain.model.StatsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val summary: StatsSummary = StatsSummary(),
    val dailyCounts: List<DailyReviewCount> = emptyList(),
    val cardsWithStats: List<CardWithStats> = emptyList(),
    val error: String? = null,
    // Batch delete
    val isSelectionMode: Boolean = false,
    val selectedCardIds: Set<Long> = emptySet(),
    val isDeleting: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val summary = statsRepository.getStatsSummary()
                val cardsWithStats = statsRepository.getCardsWithStats()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        cardsWithStats = cardsWithStats
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "加载统计失败")
                }
            }
        }

        viewModelScope.launch {
            statsRepository.getDailyReviewCounts(7).collect { counts ->
                _uiState.update { it.copy(dailyCounts = counts) }
            }
        }
    }

    fun refresh() {
        loadStats()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // === Selection & Deletion ===

    fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true, selectedCardIds = emptySet()) }
    }

    fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedCardIds = emptySet()) }
    }

    fun toggleCardSelection(cardId: Long) {
        _uiState.update { state ->
            val newSet = state.selectedCardIds.toMutableSet()
            if (newSet.contains(cardId)) newSet.remove(cardId) else newSet.add(cardId)
            state.copy(selectedCardIds = newSet)
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            state.copy(selectedCardIds = state.cardsWithStats.map { it.card.id }.toSet())
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            try {
                val card = _uiState.value.cardsWithStats
                    .firstOrNull { it.card.id == cardId }?.card ?: return@launch
                cardRepository.deleteCard(card)
                loadStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "删除失败") }
            }
        }
    }

    fun deleteSelectedCards() {
        val selectedIds = _uiState.value.selectedCardIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            try {
                val cardsToDelete = _uiState.value.cardsWithStats
                    .filter { it.card.id in selectedIds }
                    .map { it.card }
                for (card in cardsToDelete) {
                    cardRepository.deleteCard(card)
                }
                // Exit selection mode and reload
                _uiState.update { it.copy(isDeleting = false, isSelectionMode = false, selectedCardIds = emptySet()) }
                loadStats()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDeleting = false, error = e.message ?: "批量删除失败")
                }
            }
        }
    }
}
