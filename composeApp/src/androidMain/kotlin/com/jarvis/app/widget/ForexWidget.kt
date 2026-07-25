package com.jarvis.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jarvis.app.MainActivity
import kotlinx.coroutines.flow.first

class ForexWidget : GlanceAppWidget() {

    override val sizeMode = androidx.glance.appwidget.SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = ForexWidgetStore.getFlow(context).first()
        val isProfit = data.totalPnl >= 0
        val pnlColor = if (isProfit) Color(0xFF4CAF50) else Color(0xFFEF5350)
        val pnlSign = if (isProfit) "+" else ""
        val timeAgo = formatTimeAgo(data.lastUpdated)

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = "Forex PnL",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))

                        // PnL amount
                        Text(
                            text = "$pnlSign$${
                                String.format(
                                    "%,.2f",
                                    data.totalPnl
                                )
                            }",
                            style = TextStyle(
                                color = ColorProvider(pnlColor),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))

                        // Positions count
                        Text(
                            text = "${data.count} open positions",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )

                        if (data.pairsSummary.isNotEmpty()) {
                            Spacer(GlanceModifier.height(4.dp))
                            Text(
                                text = data.pairsSummary,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            text = timeAgo,
                            style = TextStyle(
                                color = GlanceTheme.colors.outline,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "—"
        val diff = System.currentTimeMillis() - timestamp
        val mins = diff / 60_000
        return when {
            mins < 1 -> "just now"
            mins < 60 -> "${mins}m ago"
            mins < 1440 -> "${mins / 60}h ago"
            else -> "${mins / 1440}d ago"
        }
    }
}
