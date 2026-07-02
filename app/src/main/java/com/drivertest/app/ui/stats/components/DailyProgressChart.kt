package com.drivertest.app.ui.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.drivertest.app.domain.model.DailyReviewCount

@Composable
fun DailyProgressChart(
    data: List<DailyReviewCount>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "每日学习进度",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                val maxCount = data.maxOfOrNull { it.count }?.toFloat() ?: 1f
                val barColor = MaterialTheme.colorScheme.primary
                val textColor = MaterialTheme.colorScheme.outline

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = data.size
                    val barWidth = (canvasWidth / barCount) * 0.6f
                    val barSpacing = (canvasWidth / barCount) * 0.4f
                    val maxBarHeight = canvasHeight - 40f

                    data.forEachIndexed { index, item ->
                        val barHeight = if (maxCount > 0) {
                            (item.count / maxCount) * maxBarHeight
                        } else 0f

                        val x = index * (barWidth + barSpacing) + barSpacing / 2f
                        val y = canvasHeight - barHeight - 20f

                        // Draw bar
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )

                        // Draw date label (MM-DD)
                        val dateLabel = if (item.date.length >= 5) {
                            item.date.substring(5) // "MM-DD"
                        } else item.date

                        drawContext.canvas.nativeCanvas.drawText(
                            dateLabel,
                            x + barWidth / 2f,
                            canvasHeight,
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )

                        // Draw count on top of bar
                        if (item.count > 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${item.count}",
                                x + barWidth / 2f,
                                y - 8f,
                                android.graphics.Paint().apply {
                                    color = barColor.hashCode()
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
