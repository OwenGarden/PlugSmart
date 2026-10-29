package com.owengarden.plugsmart.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.UsageSession
import com.owengarden.plugsmart.ui.components.SessionRow
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme

/**
 * Stateful entry point for the History screen.
 * Holds which entry the user is about to delete, so the confirm dialog survives rotation.
 */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    HistoryContent(
        uiState = uiState,
        onBack = onBack,
        onDeleteClick = { id -> pendingDeleteId = id }
    )

    // Ask before deleting, in case the bin was tapped by mistake.
    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(text = stringResource(R.string.remove_title)) },
            text = { Text(text = stringResource(R.string.remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(id)
                    pendingDeleteId = null
                }) {
                    Text(text = stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Stateless layout for the History screen:
 * 1. The user's own average price (their benchmark for days with no cheap times)
 * 2. Every finished session, newest first, each with a bin button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.personalAveragePence?.let { average ->
                item {
                    AverageCard(averagePence = average, sessionCount = uiState.pricedCount)
                }
            }
            items(uiState.sessions, key = { it.id }) { session ->
                SessionRow(session = session, onDeleteClick = { onDeleteClick(session.id) })
            }
            if (!uiState.isLoading && uiState.sessions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** The user's personal average price across their sessions. */
@Composable
private fun AverageCard(averagePence: Double, sessionCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.your_average),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.best_details, averagePence),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.your_average_details, sessionCount),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryContentPreview() {
    val hour = 60 * 60 * 1000L
    PlugSmartTheme {
        HistoryContent(
            uiState = HistoryUiState(
                isLoading = false,
                sessions = listOf(
                    UsageSession(2, "Car charging", 26 * hour, 29 * hour, 9.8, 18.0),
                    UsageSession(1, "Ran a bath", 17 * hour, 18 * hour, 34.2, 18.0)
                )
            ),
            onBack = {},
            onDeleteClick = {}
        )
    }
}
