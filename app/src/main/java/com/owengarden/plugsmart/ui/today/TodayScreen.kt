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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.owengarden.plugsmart.ui.components.ActiveSessionCard
import com.owengarden.plugsmart.ui.components.BestTimeCard
import com.owengarden.plugsmart.ui.components.DayPriceChart
import com.owengarden.plugsmart.ui.components.NowCard
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
    onOpenHistory: () -> Unit,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodayContent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onOpenHistory = onOpenHistory,
        onPowerClick = viewModel::onPowerClick,
        onRetry = viewModel::loadPrices
    )

    // Dialogs. Whether they're showing is kept in the ViewModel, so they survive rotation.
    if (uiState.showStartDialog) {
        StartSessionDialog(
            options = uiState.applianceOptions,
            onStart = viewModel::onStartSession,
            onDismiss = viewModel::onDismissStartDialog
        )
    }
    uiState.finishedSession?.let { session ->
        SessionResultDialog(session = session, onDismiss = viewModel::onDismissResult)
    }
}

/**
 * Stateless layout for the Today screen, top to bottom:
 * 1. Right now   – current price and whether to plug in
 * 2. Best time   – cheapest 3 hours left today
 * 3. Chart       – the whole day at a glance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    uiState: TodayUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenHistory: () -> Unit,
    onPowerClick: () -> Unit,
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
                    IconButton(onClick = onOpenHistory) {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = stringResource(R.string.open_history)
                        )
                    }
                    // Power button: filled in the brand colour while the power is on.
                    val powerOn = uiState.activeSession != null
                    IconButton(
                        onClick = onPowerClick,
                        colors = if (powerOn) {
                            IconButtonDefaults.filledIconButtonColors()
                        } else {
                            IconButtonDefaults.iconButtonColors()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_power),
                            contentDescription = stringResource(
                                if (powerOn) R.string.power_off else R.string.power_on
                            )
                        )
                    }
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
            PriceList(uiState = uiState, innerPadding = innerPadding)
        }
    }
}

/** The scrolling column of cards and the chart. A LazyColumn, so it scrolls in landscape too. */
@Composable
private fun PriceList(
    uiState: TodayUiState,
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
            StatusLine(
                lastUpdated = uiState.lastUpdated,
                isOffline = uiState.isOffline,
                offlineDate = uiState.offlineDate
            )
        }
        uiState.activeSession?.let { session ->
            item {
                ActiveSessionCard(session = session)
            }
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
    }
}

/**
 * Shows "Updated 12:31" when online. When offline it shows a banner saying whether
 * these are today's saved prices or an older day being used as a guide.
 */
@Composable
private fun StatusLine(lastUpdated: String?, isOffline: Boolean, offlineDate: String?) {
    if (isOffline) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = if (offlineDate != null) {
                    stringResource(R.string.offline_banner, offlineDate)
                } else {
                    stringResource(R.string.offline_today)
                },
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
            onOpenHistory = {},
            onPowerClick = {},
            onRetry = {}
        )
    }
}
