package com.owengarden.plugsmart.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.FakePriceData
import com.owengarden.plugsmart.data.findCheapestWindow
import com.owengarden.plugsmart.ui.components.BestTimeCard
import com.owengarden.plugsmart.ui.components.DayPriceChart
import com.owengarden.plugsmart.ui.components.ListHeader
import com.owengarden.plugsmart.ui.components.NowCard
import com.owengarden.plugsmart.ui.components.PriceRow
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme

/**
 * Stateful entry point for the Today screen.
 * Gets the ViewModel, collects its state, and passes it down to [TodayContent].
 * The dark-mode setting is owned by MainActivity and passed in.
 */
@Composable
fun TodayScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayContent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onCheapOnlyChanged = viewModel::onCheapOnlyChanged,
        onRetry = viewModel::loadPrices
    )
}

/**
 * Stateless layout for the Today screen, top to bottom:
 * 1. Right now   – current price and whether to plug in
 * 2. Best time   – cheapest 3 hours left today
 * 3. Chart       – the whole day at a glance
 * 4. Coming up   – list of upcoming half-hours, with a "cheap only" filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    uiState: TodayUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onCheapOnlyChanged: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Shows a sun in dark mode (tap for light) and a moon in light mode (tap for dark).
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                if (isDarkTheme) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                            ),
                            contentDescription = stringResource(
                                if (isDarkTheme) R.string.switch_to_light else R.string.switch_to_dark
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // Nothing to show yet: a spinner while loading, or an error with a Retry button.
        if (uiState.slots.isEmpty()) {
            LoadingOrError(
                isLoading = uiState.isLoading,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            PriceList(uiState = uiState, onCheapOnlyChanged = onCheapOnlyChanged, innerPadding = innerPadding)
        }
    }
}

/** The scrolling list of cards, chart and half-hour prices. */
@Composable
private fun PriceList(
    uiState: TodayUiState,
    onCheapOnlyChanged: (Boolean) -> Unit,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatusLine(lastUpdated = uiState.lastUpdated, offlineDate = uiState.offlineDate)
        }
        uiState.currentSlot?.let { slot ->
            item {
                NowCard(
                    slot = slot,
                    level = uiState.levelOf(slot),
                    bestWindow = uiState.bestWindow
                )
            }
        }
        item {
            BestTimeCard(
                window = uiState.bestWindow,
                savingPercent = uiState.bestWindowSavingPercent
            )
        }
        item {
            DayPriceChart(
                slots = uiState.slots,
                currentIndex = uiState.currentIndex,
                averagePence = uiState.averagePence
            )
        }
        item {
            ListHeader(
                cheapOnly = uiState.cheapOnly,
                onCheapOnlyChange = onCheapOnlyChanged
            )
        }
        // The start time in milliseconds is unique, so it is used as a stable key.
        items(uiState.visibleSlots, key = { it.startMillis }) { slot ->
            PriceRow(
                slot = slot,
                level = uiState.levelOf(slot),
                isNow = slot == uiState.currentSlot
            )
        }
        if (uiState.visibleSlots.isEmpty()) {
            item {
                Text(
                    text = stringResource(
                        if (uiState.cheapOnly) R.string.no_cheap_left else R.string.no_slots_left
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Shows "Updated 12:31", or an offline banner when saved prices are being used. */
@Composable
private fun StatusLine(lastUpdated: String?, offlineDate: String?) {
    if (offlineDate != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = stringResource(R.string.offline_banner, offlineDate),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    } else if (lastUpdated != null) {
        Text(
            text = stringResource(R.string.updated_at, lastUpdated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Full-screen spinner, or an error message with a Retry button if loading failed. */
@Composable
private fun LoadingOrError(isLoading: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.error_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayContentPreview() {
    val slots = FakePriceData.todaysPrices()
    val nowIndex = 27 // 13:30
    PlugSmartTheme {
        TodayContent(
            uiState = TodayUiState(
                isLoading = false,
                lastUpdated = "12:31",
                slots = slots,
                currentIndex = nowIndex,
                averagePence = slots.map { it.pricePence }.average(),
                bestWindow = findCheapestWindow(slots.drop(nowIndex), slotCount = 6)
            ),
            isDarkTheme = false,
            onToggleTheme = {},
            onCheapOnlyChanged = {},
            onRetry = {}
        )
    }
}
