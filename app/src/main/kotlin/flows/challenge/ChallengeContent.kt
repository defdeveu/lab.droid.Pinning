package defdev.labs.droid.pinning.flows.challenge

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
import defdev.labs.droid.pinning.ui.SecondaryAction
import defdev.labs.droid.pinning.ui.SectionCard

@Composable
fun ChallengeContent(viewModel: PinningViewModel, active: Boolean) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestPage(stringResource(R.string.flow_challenge_title), stringResource(R.string.pinning_supporting)) {
        SectionCard(stringResource(R.string.section_expected_pin)) {
            OutputPane(state.expectedPin, stringResource(R.string.placeholder_expected_pin))
        }
        RequestAction(state.isConfigured, state.isRunning,
            { if (active) viewModel.connect() }, { if (active) viewModel.cancel() })
        SecondaryAction(stringResource(R.string.action_try_cleartext),
            { if (active) viewModel.tryCleartext() }, enabled = !state.isRunning && state.isConfigured)
        RequestResult(state.status, state.result, state.error, state.leafPin)
        ConfigurationError(state.configurationError)
    }
}
