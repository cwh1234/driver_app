package com.drivertest.app.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.drivertest.app.domain.model.ReviewStatus
import com.drivertest.app.ui.components.EmptyStateView
import com.drivertest.app.ui.components.ErrorDialog
import com.drivertest.app.ui.theme.MasteredGreen
import com.drivertest.app.ui.theme.MasteredGreenLight
import com.drivertest.app.ui.theme.NotFamiliarRed
import com.drivertest.app.ui.theme.NotFamiliarRedLight
import com.drivertest.app.ui.theme.UnclearOrange
import com.drivertest.app.ui.theme.UnclearOrangeLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    viewModel: LearnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload review queue whenever this screen enters the RESUMED state,
    // including after navigating back from other tabs (e.g. after creating cards).
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadReviewQueue()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress indicator
        if (!uiState.isEmpty && !uiState.isComplete && uiState.currentCard != null) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日已复习 ${uiState.todayReviewed} 张",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.queuePosition}/${uiState.queueSize}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        if (uiState.queueSize > 0)
                            uiState.queuePosition.toFloat() / uiState.queueSize
                        else 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val currentCard = uiState.currentCard
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.isEmpty -> {
                    EmptyStateView(
                        title = "暂无知识卡片",
                        subtitle = "去添加页面创建你的第一张知识卡片吧!"
                    )
                }

                uiState.isComplete -> {
                    // All reviewed - celebration state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MasteredGreen
                        )
                        Text(
                            text = "今日复习完成! 🎉",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "已复习 ${uiState.todayReviewed} 张卡片，明天继续加油!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.refreshQueue() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重新复习")
                        }
                    }
                }

                currentCard != null -> {
                    // Active card
                    KnowledgeCardContent(
                        card = currentCard,
                        onSwipeLeft = { viewModel.markStatus(ReviewStatus.NOT_FAMILIAR) },
                        onSwipeRight = { viewModel.markStatus(ReviewStatus.MASTERED) }
                    )
                }
            }
        }

        // Bottom action buttons (only when a card is active)
        if (uiState.currentCard != null && !uiState.isComplete) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 不熟悉 button
                Button(
                    onClick = { viewModel.markStatus(ReviewStatus.NOT_FAMILIAR) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NotFamiliarRedLight,
                        contentColor = NotFamiliarRed
                    )
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("不熟悉")
                }

                // 模糊 button
                Button(
                    onClick = { viewModel.markStatus(ReviewStatus.UNCLEAR) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UnclearOrangeLight,
                        contentColor = UnclearOrange
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("模糊")
                }

                // 掌握 button
                Button(
                    onClick = { viewModel.markStatus(ReviewStatus.MASTERED) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MasteredGreenLight,
                        contentColor = MasteredGreen
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("掌握")
                }
            }
        }

        // Empty space when all reviewed (to keep layout stable)
        if (uiState.isComplete) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Error dialog
    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = { viewModel.clearError() },
            onRetry = { viewModel.loadReviewQueue() }
        )
    }
}
