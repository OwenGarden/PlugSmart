package com.owengarden.plugsmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.CheapestWindow
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.data.PriceSlot
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme

/*
 * All composables in this file are stateless: they only display what they are given
 * and report user actions through callbacks. State is hoisted to the screen/ViewModel.
 */

/** A rounded coloured "pill" showing a price, e.g. [ 8.2p ] in green. */
@Composable
fun PricePill(pricePence: Double, level: PriceLevel, modifier: Modifier = Modifier) {
    val style = level.style()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = style.colour
    ) {
        Text(
            text = stringResource(R.string.price_pence, pricePence),
            color = style.onColour,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * The first thing the user sees: the price right now and a plain-English answer
 * to "should I plug in now?". The card is tinted with the price level's colour.
 */
@Composable
fun NowCard(
    slot: PriceSlot,
    level: PriceLevel,
    bestWindow: CheapestWindow?,
    modifier: Modifier = Modifier
) {
    val style = level.style()
    val advice = when (level) {
        PriceLevel.CHEAP -> R.string.advice_cheap
        PriceLevel.NORMAL -> R.string.advice_normal
        PriceLevel.EXPENSIVE -> R.string.advice_expensive
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = style.colour.copy(alpha = 0.15f)),
        border = BorderStroke(2.dp, style.colour)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.now_label, slot.startTime, slot.endTime),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.price_pence, slot.pricePence),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.per_kwh),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(advice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            // If it isn't cheap right now, point the user to a better time.
            if (level != PriceLevel.CHEAP && bestWindow != null && bestWindow.startTime != slot.startTime) {
                Text(
                    text = stringResource(R.string.cheaper_from, bestWindow.startTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Highlights the cheapest 3-hour block left today and what it's good for. */
@Composable
fun BestTimeCard(window: CheapestWindow?, savingPercent: Int?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.best_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (window == null) {
                Text(text = stringResource(R.string.no_window))
            } else {
                Text(
                    text = stringResource(R.string.time_range, window.startTime, window.endTime),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stringResource(R.string.best_details, window.averagePence))
                if (savingPercent != null) {
                    Text(
                        text = stringResource(R.string.saving, savingPercent),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.best_examples),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Section title plus the switch that filters the list down to cheap slots only. */
@Composable
fun ListHeader(
    cheapOnly: Boolean,
    onCheapOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.coming_up),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.cheap_only),
                modifier = Modifier.weight(1f)
            )
            Switch(checked = cheapOnly, onCheckedChange = onCheapOnlyChange)
        }
    }
}

/** One row in the price list: time, label and a coloured price pill. The current slot gets a border. */
@Composable
fun PriceRow(slot: PriceSlot, level: PriceLevel, isNow: Boolean, modifier: Modifier = Modifier) {
    val label = stringResource(level.style().label)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (isNow) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.time_range, slot.startTime, slot.endTime),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isNow) stringResource(R.string.now_level, label) else label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isNow) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
                )
            }
            PricePill(pricePence = slot.pricePence, level = level)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NowCardPreview() {
    PlugSmartTheme {
        NowCard(
            slot = PriceSlot("17:00", "17:30", 35.2),
            level = PriceLevel.EXPENSIVE,
            bestWindow = CheapestWindow("21:00", "00:00", 15.9)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceRowPreview() {
    PlugSmartTheme {
        PriceRow(slot = PriceSlot("02:00", "02:30", 8.2), level = PriceLevel.CHEAP, isNow = true)
    }
}
