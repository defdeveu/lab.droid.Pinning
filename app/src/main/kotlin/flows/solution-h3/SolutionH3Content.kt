package defdev.labs.droid.pinning.flows.solutionh3

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labs.droid.pinning.R
import defdev.labs.droid.pinning.ui.OutputPane
import defdev.labs.droid.pinning.ui.RequestAction
import defdev.labs.droid.pinning.ui.RequestPage
import defdev.labs.droid.pinning.ui.RequestResult
import defdev.labs.droid.pinning.ui.SectionCard

@Composable
fun SolutionH3Content(viewModel: PinningViewModel, active: Boolean) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestPage(stringResource(R.string.flow_solution_h3_title),
        stringResource(R.string.solution_h3_supporting, state.pinnedHost)) {
        SectionCard(stringResource(R.string.section_pinned_identities)) {
            state.pinnedDigests.forEach { OutputPane("sha256/$it", "") }
            Text(stringResource(R.string.pinned_identities_note), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RequestAction(viewModel.isAvailable, state.isRunning,
            { if (active) viewModel.connect() }, { if (active) viewModel.cancel() })
        RequestResult(state.status, state.result, state.error)
    }
}
