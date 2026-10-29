package com.owengarden.plugsmart.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.data.UsageRepository
import com.owengarden.plugsmart.data.UsageSession
import com.owengarden.plugsmart.data.priceLevel
import com.owengarden.plugsmart.ui.components.formatDuration
import com.owengarden.plugsmart.ui.components.formatTime

/**
 * Asks what the user is about to use. They can tap one of the [options]
 * (built-in ones plus any they've typed before) or type their own.
 * rememberSaveable keeps the choice and typed text if the screen rotates.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartSessionDialog(
    options: List<String>,
    onStart: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var custom by rememberSaveable { mutableStateOf("") }

    // Typed text wins over a tapped chip. Null means nothing chosen yet.
    val label: String? = if (custom.isNotBlank()) custom.trim() else selected

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.start_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        FilterChip(
                            selected = custom.isBlank() && option == selected,
                            onClick = {
                                selected = option
                                custom = ""
                            },
                            label = { Text(text = option) }
                        )
                    }
                }
                OutlinedTextField(
                    value = custom,
                    // Limit the length here as well as in the repository.
                    onValueChange = { custom = it.take(UsageRepository.MAX_LABEL_LENGTH) },
                    label = { Text(text = stringResource(R.string.start_custom)) },
                    placeholder = { Text(text = stringResource(R.string.start_custom_hint)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { label?.let(onStart) },
                enabled = label != null
            ) {
                Text(text = stringResource(R.string.start))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

/** Shown when the power is turned off: when it was used and what it cost compared with today. */
@Composable
fun SessionResultDialog(session: UsageSession, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = session.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        R.string.result_times,
                        formatTime(session.startMillis),
                        formatTime(session.endMillis ?: session.startMillis),
                        formatDuration(session.durationMillis)
                    )
                )
                val average = session.averagePence
                val dayAverage = session.dayAveragePence
                if (average != null) {
                    Text(
                        text = stringResource(R.string.result_price, average),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (dayAverage != null && dayAverage > 0) {
                        val levelWord = when (priceLevel(average, dayAverage)) {
                            PriceLevel.CHEAP -> R.string.level_cheap_lower
                            PriceLevel.NORMAL -> R.string.level_normal_lower
                            PriceLevel.EXPENSIVE -> R.string.level_expensive_lower
                        }
                        Text(text = stringResource(R.string.result_compared, stringResource(levelWord), dayAverage))
                    }
                } else {
                    Text(text = stringResource(R.string.price_unknown))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok))
            }
        }
    )
}
