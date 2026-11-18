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
import defdev.labs.droid.pinning.flows.solutionh3.PinningViewModel as H3ViewModel
import defdev.labs.droid.pinning.flows.solutionh3.SolutionH3Content

@Composable
fun PinningApp(challenge: ChallengeViewModel, solution: SolutionViewModel, h3: H3ViewModel? = null) {
    var selectedId by rememberSaveable { mutableStateOf(FlowId.CHALLENGE.value) }
    val selected = FlowId.entries.first { it.value == selectedId }
    val challengeState by challenge.uiState.collectAsStateWithLifecycle()
    val solutionState by solution.uiState.collectAsStateWithLifecycle()
    val h3Running = h3?.uiState?.collectAsStateWithLifecycle()?.value?.isRunning == true
    val h3Available = h3?.isAvailable == true
    val displayed = if (selected == FlowId.SOLUTION_H3 && !h3Available) FlowId.CHALLENGE else selected
    val isRunning = challengeState.isRunning || solutionState.isRunning || h3Running
    PinningScreen(
        selected = displayed,
        onSelect = { flow ->
            if (!challenge.uiState.value.isRunning && !solution.uiState.value.isRunning &&
                h3?.uiState?.value?.isRunning != true && (flow != FlowId.SOLUTION_H3 || h3Available)) selectedId = flow.value
        },
        isRunning = isRunning,
        h3Available = h3Available,
        challenge = { active -> ChallengeContent(challenge, active) },
        solution = { active -> SolutionContent(solution, active) },
        solutionH3 = { active -> if (h3 != null) SolutionH3Content(h3, active) },
    )
}
