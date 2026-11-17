package defdev.labs.droid.pinning.flows.solution

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labs.droid.pinning.R
import defdev.labs.droid.pinning.ui.ConfigurationError
import defdev.labs.droid.pinning.ui.OutputPane
import defdev.labs.droid.pinning.ui.RequestAction
import defdev.labs.droid.pinning.ui.RequestPage
import defdev.labs.droid.pinning.ui.RequestResult
import defdev.labs.droid.pinning.ui.SectionCard

@Composable
fun SolutionContent(viewModel: PinningViewModel, active: Boolean) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestPage(stringResource(R.string.flow_solution_title),
        stringResource(R.string.solution_supporting, state.pinnedHost)) {
        SectionCard(stringResource(R.string.section_pinned_identities)) {
            if (state.pinnedDigests.isEmpty()) {
                OutputPane("", stringResource(R.string.placeholder_pinned_identities))
            } else {
                state.pinnedDigests.forEach { OutputPane("sha256/$it", "") }
                Text(stringResource(R.string.pinned_identities_note), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        RequestAction(state.isConfigured, state.isRunning,
            { if (active) viewModel.connect() }, { if (active) viewModel.cancel() })
        RequestResult(state.status, state.result, state.error)
        ConfigurationError(state.configurationError)
    }
}
