package defdev.labs.droid.pinning.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import defdev.labs.droid.pinning.R
import defdev.labs.droid.pinning.flows.FlowId

@Composable
fun PinningScreen(
    selected: FlowId,
    onSelect: (FlowId) -> Unit,
    isRunning: Boolean,
    challenge: @Composable (Boolean) -> Unit,
    solution: @Composable (Boolean) -> Unit,
) {
    val motion = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    Scaffold(topBar = { LabHeader() }) { insets ->
        Row(Modifier.fillMaxSize().padding(insets)) {
            WideNavigationRail(
                modifier = Modifier.fillMaxHeight().width(76.8.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = WideNavigationRailDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                FlowId.entries.forEach { flow ->
                    val label = stringResource(flow.titleRes())
                    WideNavigationRailItem(
                        selected = selected == flow,
                        onClick = { if (!isRunning) onSelect(flow) },
                        enabled = !isRunning,
                        icon = {
                            Text(stringResource(flow.markerRes()),
                                style = MaterialTheme.typography.titleMediumEmphasized, fontSize = 20.sp)
                        },
                        label = null,
                        railExpanded = false,
                        modifier = Modifier.semantics { contentDescription = label },
                    )
                }
            }
            AnimatedContent(
                targetState = selected,
                modifier = Modifier.weight(1f).fillMaxHeight().clipToBounds(),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                    slideIntoContainer(direction, motion) togetherWith slideOutOfContainer(direction, motion)
                },
                label = "caseTransition",
            ) { page ->
                when (page) {
                    FlowId.CHALLENGE -> challenge(page == selected)
                    FlowId.SOLUTION -> solution(page == selected)
                }
            }
        }
    }
}

private fun FlowId.titleRes() = when (this) {
    FlowId.CHALLENGE -> R.string.flow_challenge_title
    FlowId.SOLUTION -> R.string.flow_solution_title
}

private fun FlowId.markerRes() = when (this) {
    FlowId.CHALLENGE -> R.string.flow_challenge_marker
    FlowId.SOLUTION -> R.string.flow_solution_marker
}
