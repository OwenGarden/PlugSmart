package com.owengarden.plugsmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.UsageSession
import com.owengarden.plugsmart.data.priceLevel
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
 * Stateless composables for the power button sessions, used by both screens.
 */

private val ukZone = ZoneId.of("Europe/London")

/** Milliseconds -> "14:30" in UK time. */
fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ukZone).format(DateTimeFormatter.ofPattern("HH:mm"))

/** Milliseconds -> "Tue 28 Oct" in UK time. */
fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ukZone).format(DateTimeFormatter.ofPattern("EEE d MMM"))

/** Milliseconds -> "3h 30m" or "45m". */
@Composable
fun formatDuration(millis: Long): String {
    val totalMinutes = (millis / 60_000).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.duration_minutes, minutes)
    }
}

/** Shown at the top of the Today screen while the power button is on. */
@Composable
fun ActiveSessionCard(session: UsageSession, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_power),
                contentDescription = null, // decorative: the text says the same thing
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.active_title, session.label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.active_since, formatTime(session.startMillis)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.active_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** One finished session in the history list, with a bin button to remove it. */
@Composable
fun SessionRow(session: UsageSession, onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        R.string.session_when,
                        formatDate(session.startMillis),
                        formatTime(session.startMillis),
                        formatTime(session.endMillis ?: session.startMillis),
                        formatDuration(session.durationMillis)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val average = session.averagePence
            if (average != null) {
                PricePill(
                    pricePence = average,
                    level = priceLevel(average, session.dayAveragePence ?: average)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.remove_entry)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SessionRowPreview() {
    PlugSmartTheme {
        SessionRow(
            session = UsageSession(
                id = 1,
                label = "Car charging",
                startMillis = 0L,
                endMillis = 3 * 60 * 60 * 1000L,
                averagePence = 9.8,
                dayAveragePence = 18.0
            ),
            onDeleteClick = {}
        )
    }
}
