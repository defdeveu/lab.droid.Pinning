package dev.def.lab.droid.pinning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import dev.def.lab.droid.pinning.PinStatus
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
                        text = stringResource(R.string.pinning_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SectionCard(title = stringResource(R.string.section_expected_pin)) {
                    OutputPane(
                        text = state.expectedPin,
                        placeholder = stringResource(R.string.placeholder_expected_pin),
                    )
                }

                PrimaryAction(
                    label = stringResource(
                        if (state.isRunning) R.string.action_connecting else R.string.action_connect,
                    ),
                    onClick = viewModel::connect,
                    enabled = !state.isRunning && state.isConfigured,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SecondaryAction(
                        label = stringResource(R.string.action_break_pin),
                        onClick = viewModel::breakPin,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isRunning,
                    )
                    SecondaryAction(
                        label = stringResource(R.string.action_restore_pin),
                        onClick = viewModel::restorePin,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isRunning,
                    )
                }

                SecondaryAction(
                    label = stringResource(R.string.action_try_cleartext),
                    onClick = viewModel::tryCleartext,
                    enabled = !state.isRunning && state.isConfigured,
                )

                SectionCard(title = stringResource(R.string.section_status)) {
                    Text(
                        text = stringResource(state.status.labelRes()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                SectionCard(title = stringResource(R.string.section_served_pin)) {
                    OutputPane(
                        text = state.leafPin,
                        placeholder = stringResource(R.string.placeholder_served_pin),
                    )
                }

                SectionCard(title = stringResource(R.string.section_last_attempt)) {
                    OutputPane(
                        text = state.result,
                        placeholder = stringResource(R.string.placeholder_last_attempt),
                    )
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

                state.error?.let { error ->
                    Text(
                        text = stringResource(error.messageRes(), error.statusCode()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@StringRes
private fun PinStatus.labelRes(): Int = when (this) {
    PinStatus.READY -> R.string.status_ready
    PinStatus.CONNECTING -> R.string.status_connecting
    PinStatus.CONNECTED -> R.string.status_connected
    PinStatus.FAILED -> R.string.status_failed
}

@StringRes
private fun LabError.messageRes(): Int = when (this) {
    LabError.InvalidConfiguration -> R.string.error_invalid_configuration
    LabError.PinNotConfigured -> R.string.error_pin_not_configured
    is LabError.PinMismatch -> R.string.error_pin_mismatch
    LabError.TrustFailed -> R.string.error_trust_failed
    LabError.InvalidUrl -> R.string.error_invalid_url
    LabError.TransportFailed -> R.string.error_transport_failed
    is LabError.ServerStatus -> R.string.error_server_status
}

private fun LabError.statusCode(): Int = (this as? LabError.ServerStatus)?.status ?: 0
