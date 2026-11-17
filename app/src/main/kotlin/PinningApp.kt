package defdev.labs.droid.pinning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import defdev.labs.droid.pinning.flows.FlowId
import defdev.labs.droid.pinning.flows.challenge.ChallengeContent
import defdev.labs.droid.pinning.flows.solution.SolutionContent
import defdev.labs.droid.pinning.ui.PinningScreen
import defdev.labs.droid.pinning.flows.challenge.PinningViewModel as ChallengeViewModel
import defdev.labs.droid.pinning.flows.solution.PinningViewModel as SolutionViewModel

@Composable
fun PinningApp(challenge: ChallengeViewModel, solution: SolutionViewModel) {
    var selectedId by rememberSaveable { mutableStateOf(FlowId.CHALLENGE.value) }
    val selected = FlowId.entries.first { it.value == selectedId }
    val challengeState by challenge.uiState.collectAsStateWithLifecycle()
    val solutionState by solution.uiState.collectAsStateWithLifecycle()
    val isRunning = challengeState.isRunning || solutionState.isRunning
    PinningScreen(
        selected = selected,
        onSelect = { flow ->
            if (!challenge.uiState.value.isRunning && !solution.uiState.value.isRunning) selectedId = flow.value
        },
        isRunning = isRunning,
        challenge = { active -> ChallengeContent(challenge, active) },
        solution = { active -> SolutionContent(solution, active) },
    )
}
