package defdev.labs.droid.pinning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import defdev.labs.droid.pinning.PinStatus
import defdev.labs.droid.pinning.R
import defdev.labs.droid.pinning.error.LabError

@Composable
fun RequestPage(title: String, supporting: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleLargeEmphasized)
                Text(supporting, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
fun RequestAction(isConfigured: Boolean, isRunning: Boolean, onConnect: () -> Unit, onCancel: () -> Unit) {
    PrimaryAction(
        label = stringResource(if (isRunning) R.string.action_cancel_request else R.string.action_connect),
        onClick = if (isRunning) onCancel else onConnect,
        enabled = isConfigured || isRunning,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestResult(status: PinStatus, result: String, error: LabError?, servedPin: String? = null) {
    SectionCard(stringResource(R.string.section_status)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (status == PinStatus.CONNECTING) LoadingIndicator()
            Text(stringResource(status.labelRes()), style = MaterialTheme.typography.bodyLarge)
        }
        if (servedPin != null) {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text(stringResource(R.string.section_served_pin), style = MaterialTheme.typography.titleSmallEmphasized)
            OutputPane(servedPin, stringResource(R.string.placeholder_served_pin))
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        Text(stringResource(R.string.section_last_attempt), style = MaterialTheme.typography.titleSmallEmphasized)
        when {
            error != null -> Text(stringResource(error.messageRes(), error.statusCode()),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            status == PinStatus.CANCELLED -> Text(stringResource(R.string.status_cancelled),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> OutputPane(result, stringResource(R.string.placeholder_last_attempt))
        }
    }
}

@Composable
fun ConfigurationError(error: LabError?) {
    if (error != null) {
        Text(stringResource(R.string.configuration_error, stringResource(error.messageRes(), error.statusCode())),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

@StringRes
private fun PinStatus.labelRes() = when (this) {
    PinStatus.READY -> R.string.status_ready
    PinStatus.CONNECTING -> R.string.status_connecting
    PinStatus.CONNECTED -> R.string.status_connected
    PinStatus.FAILED -> R.string.status_failed
    PinStatus.CANCELLED -> R.string.status_cancelled
}

@StringRes
private fun LabError.messageRes() = when (this) {
    LabError.InvalidConfiguration -> R.string.error_invalid_configuration
    LabError.PinNotConfigured -> R.string.error_pin_not_configured
    is LabError.PinMismatch -> R.string.error_pin_mismatch
    LabError.TrustFailed -> R.string.error_trust_failed
    LabError.InvalidUrl -> R.string.error_invalid_url
    LabError.TransportFailed -> R.string.error_transport_failed
    LabError.Http3Required -> R.string.error_http3_required
    is LabError.ServerStatus -> R.string.error_server_status
}

private fun LabError.statusCode() = (this as? LabError.ServerStatus)?.status ?: 0
