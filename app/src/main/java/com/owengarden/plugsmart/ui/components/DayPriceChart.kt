package com.owengarden.plugsmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.FakePriceData
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.data.PriceSlot
import com.owengarden.plugsmart.data.priceLevel
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme

/**
 * A simple bar chart of the whole day, built from plain Boxes (no chart library).
 *
 * - Each bar is one half-hour slot; its height is the price compared to the day's highest.
 * - Bars are coloured cheap/normal/expensive; slots that have already passed are faded.
 * - A small line under the bars marks the current time.
 */
@Composable
fun DayPriceChart(
    slots: List<PriceSlot>,
    currentIndex: Int,
    averagePence: Double,
    modifier: Modifier = Modifier
) {
    // Avoid dividing by zero (or a negative number if prices ever go below zero).
    val maxPrice = slots.maxOfOrNull { it.pricePence }?.takeIf { it > 0 } ?: 1.0
    val chartDescription = stringResource(R.string.chart_description)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.chart_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // The bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .semantics { contentDescription = chartDescription },
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                slots.forEachIndexed { index, slot ->
                    val heightFraction = (slot.pricePence / maxPrice).toFloat().coerceIn(0.05f, 1f)
                    val colour = priceLevel(slot.pricePence, averagePence).style().colour
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFraction)
                            .background(
                                color = if (index < currentIndex) colour.copy(alpha = 0.3f) else colour,
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }

            // "Now" marker, lined up under the current bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                slots.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (index == currentIndex) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                    )
                }
            }

            // Time labels along the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ChartLegend()
            Text(
                text = stringResource(R.string.average_today, averagePence),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Explains what the colours and the marker mean. */
@Composable
private fun ChartLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        PriceLevel.entries.forEach { level ->
            val style = level.style()
            LegendItem(colour = style.colour, label = stringResource(style.label))
        }
        LegendItem(colour = MaterialTheme.colorScheme.primary, label = stringResource(R.string.legend_now))
    }
}

@Composable
private fun LegendItem(colour: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(colour, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun DayPriceChartPreview() {
    val slots = FakePriceData.todaysPrices()
    PlugSmartTheme {
        DayPriceChart(
            slots = slots,
            currentIndex = 27,
            averagePence = slots.map { it.pricePence }.average()
        )
    }
}
