package com.drivertest.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drivertest.app.ui.components.EmptyStateView
import com.drivertest.app.ui.components.ErrorDialog
import com.drivertest.app.ui.stats.components.CardStatsListItem
import com.drivertest.app.ui.stats.components.DailyProgressChart
import com.drivertest.app.ui.stats.components.StatsOverviewCards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }

    // Delete confirmation dialog (single card)
    if (showDeleteConfirmDialog && deleteTargetId != null) {
        val targetCard = uiState.cardsWithStats.firstOrNull { it.card.id == deleteTargetId }
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                deleteTargetId = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除卡片「${targetCard?.card?.title ?: ""}」吗？\n删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    deleteTargetId?.let { viewModel.deleteCard(it) }
                    showDeleteConfirmDialog = false
                    deleteTargetId = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    deleteTargetId = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Batch delete confirmation dialog
    if (showDeleteConfirmDialog && deleteTargetId == null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("批量删除") },
            text = { Text("确定要删除选中的 ${uiState.selectedCardIds.size} 张卡片吗？\n删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelectedCards()
                    showDeleteConfirmDialog = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    if (uiState.isSelectionMode)
                        "已选 ${uiState.selectedCardIds.size} 张"
                    else
                        "学习统计"
                )
            },
            navigationIcon = {
                if (uiState.isSelectionMode) {
                    IconButton(onClick = { viewModel.exitSelectionMode() }) {
                        Icon(Icons.Default.Deselect, contentDescription = "取消选择")
                    }
                }
            },
            actions = {
                if (uiState.isSelectionMode) {
                    // Select all / deselect all
                    val allSelected = uiState.selectedCardIds.size == uiState.cardsWithStats.size
                    IconButton(onClick = {
                        if (allSelected) viewModel.exitSelectionMode()
                        else viewModel.selectAll()
                    }) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = if (allSelected) "取消全选" else "全选"
                        )
                    }
                    // Batch delete
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = uiState.selectedCardIds.isNotEmpty() && !uiState.isDeleting
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "批量删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    // Normal mode actions
                    IconButton(onClick = { viewModel.enterSelectionMode() }) {
                        Icon(Icons.Default.Delete, contentDescription = "批量删除")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                ErrorDialog(
                    message = uiState.error!!,
                    onDismiss = { viewModel.clearError() },
                    onRetry = { viewModel.loadStats() }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overview cards
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        StatsOverviewCards(summary = uiState.summary)
                    }

                    // Daily progress chart
                    item {
                        DailyProgressChart(data = uiState.dailyCounts)
                    }

                    // Card details header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "卡片详情",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!uiState.isSelectionMode) {
                                TextButton(onClick = { viewModel.enterSelectionMode() }) {
                                    Text("批量管理", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Card stats list
                    if (uiState.cardsWithStats.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "暂无学习数据",
                                subtitle = "开始学习后这里会显示每张卡片的统计信息"
                            )
                        }
                    } else {
                        items(
                            items = uiState.cardsWithStats,
                            key = { it.card.id }
                        ) { cardWithStats ->
                            CardStatsListItem(
                                item = cardWithStats,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = cardWithStats.card.id in uiState.selectedCardIds,
                                onToggleSelection = {
                                    viewModel.toggleCardSelection(cardWithStats.card.id)
                                },
                                onDelete = {
                                    deleteTargetId = cardWithStats.card.id
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
