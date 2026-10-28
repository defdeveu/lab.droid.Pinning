package dev.def.lab.droid.pinning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.def.lab.droid.pinning.ConnectStatus
import dev.def.lab.droid.pinning.PinningViewModel
import dev.def.lab.droid.pinning.R
import dev.def.lab.droid.pinning.error.LabError

@Composable
fun PinningScreen(viewModel: PinningViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.pinning_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.pinning_supporting, state.pinnedHost),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SectionCard(title = stringResource(R.string.section_pinned_identities)) {
                    if (state.pinnedDigests.isEmpty()) {
                        OutputPane(
                            text = "",
                            placeholder = stringResource(R.string.placeholder_pinned_identities),
                        )
                    } else {
                        state.pinnedDigests.forEach { digest ->
                            OutputPane(text = "sha256/$digest", placeholder = "")
                        }
                        Text(
                            text = stringResource(R.string.pinned_identities_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                PrimaryAction(
                    label = stringResource(
                        if (state.isRunning) R.string.action_connecting else R.string.action_connect,
                    ),
                    onClick = viewModel::connect,
                    enabled = !state.isRunning && state.isConfigured,
                )

                SectionCard(title = stringResource(R.string.section_status)) {
                    Text(
                        text = stringResource(state.status.labelRes()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                SectionCard(title = stringResource(R.string.section_last_attempt)) {
                    val failure = state.error
                    if (failure != null) {
                        Text(
                            text = stringResource(failure.messageRes(), failure.statusCode()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        OutputPane(
                            text = state.result,
                            placeholder = stringResource(R.string.placeholder_last_attempt),
                        )
                    }
                }

                state.configurationError?.let { error ->
                    Text(
                        text = stringResource(
                            R.string.configuration_error,
                            stringResource(error.messageRes(), error.statusCode()),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@StringRes
private fun ConnectStatus.labelRes(): Int = when (this) {
    ConnectStatus.READY -> R.string.status_ready
    ConnectStatus.CONNECTING -> R.string.status_connecting
    ConnectStatus.CONNECTED -> R.string.status_connected
    ConnectStatus.FAILED -> R.string.status_failed
}

@StringRes
private fun LabError.messageRes(): Int = when (this) {
    LabError.InvalidConfiguration -> R.string.error_invalid_configuration
    LabError.InvalidUrl -> R.string.error_invalid_url
    LabError.TransportFailed -> R.string.error_transport_failed
    LabError.TrustFailed -> R.string.error_trust_failed
    is LabError.ServerStatus -> R.string.error_server_status
}

private fun LabError.statusCode(): Int = (this as? LabError.ServerStatus)?.status ?: 0
