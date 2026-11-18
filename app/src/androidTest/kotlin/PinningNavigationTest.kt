package defdev.labs.droid.pinning

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import defdev.labs.droid.pinning.flows.solution.PinnedSet
import defdev.labs.droid.pinning.flows.FlowId
import defdev.labs.droid.pinning.flows.solutionh3.PinningViewModel as H3ViewModel
import defdev.labs.droid.pinning.flows.solutionh3.PinningService as H3Service
import defdev.labs.droid.pinning.flows.solutionh3.FetchResult as H3Result
import defdev.labs.droid.pinning.theme.PinningTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import defdev.labs.droid.pinning.flows.challenge.FetchResult as ChallengeResult
import defdev.labs.droid.pinning.flows.challenge.PinningService as ChallengeService
import defdev.labs.droid.pinning.flows.challenge.PinningViewModel as ChallengeViewModel
import defdev.labs.droid.pinning.flows.solution.FetchResult as SolutionResult
import defdev.labs.droid.pinning.flows.solution.PinningService as SolutionService
import defdev.labs.droid.pinning.flows.solution.PinningViewModel as SolutionViewModel

class PinningNavigationTest {
    @get:Rule val rule = createComposeRule()
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test fun challengeRequestLocksTheRailUntilCancelled() {
        verifyCancellation(FlowId.CHALLENGE)
    }

    @Test fun solutionRequestLocksTheRailUntilCancelled() {
        verifyCancellation(FlowId.SOLUTION)
    }

    @Test fun thirdRequestLocksTheRailUntilCancelled() {
        verifyCancellation(FlowId.SOLUTION_H3)
    }

    @Test fun anUnavailableThirdCaseKeepsItsRailItemDisabled() {
        verifyCancellation(FlowId.CHALLENGE, h3Available = false)
    }

    private fun verifyCancellation(selectedFlow: FlowId, h3Available: Boolean = true) {
        val challengeService = WaitingChallengeService()
        val solutionService = WaitingSolutionService()
        val h3Service = WaitingH3Service()
        val challenge = ChallengeViewModel(challengeService,
            "https://lab.example/success", "http://lab.example/success", BuildConfig.LAB_LEAF_PIN, Dispatchers.Main)
        val solution = SolutionViewModel(solutionService, "https://other.example/success",
            PinnedSet("other.example", listOf("first", "second")), Dispatchers.Main)
        val h3 = H3ViewModel(h3Service, "https://other.example/pinning/h3",
            PinnedSet("other.example", listOf("first", "second")), h3Available, Dispatchers.Main)
        rule.setContent { PinningTheme { PinningApp(challenge, solution, h3) } }
        val selected = when (selectedFlow) {
            FlowId.CHALLENGE -> R.string.flow_challenge_title
            FlowId.SOLUTION -> R.string.flow_solution_title
            FlowId.SOLUTION_H3 -> R.string.flow_solution_h3_title
        }
        val other = if (selectedFlow == FlowId.CHALLENGE) R.string.flow_solution_title else R.string.flow_challenge_title
        if (!h3Available) rule.onNodeWithContentDescription(text(R.string.flow_solution_h3_title)).assertIsNotEnabled()
        if (selectedFlow != FlowId.CHALLENGE) rule.onNodeWithContentDescription(text(selected)).performClick()
        rule.onNodeWithText(text(R.string.action_connect)).performScrollTo().performClick()
        rule.onNodeWithContentDescription(text(selected)).assertIsSelected().assertIsNotEnabled()
        rule.onNodeWithContentDescription(text(other)).assertIsNotEnabled().performClick()
        rule.onNodeWithContentDescription(text(selected)).assertIsSelected()
        rule.onNodeWithText(text(R.string.action_cancel_request)).performScrollTo().assertIsEnabled()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir, "${selectedFlow.value}-request.png")
            .outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        rule.onNodeWithText(text(R.string.action_cancel_request)).performClick()
        rule.onNodeWithText(text(R.string.action_cancel_request)).assertDoesNotExist()
        rule.onNodeWithContentDescription(text(other)).assertIsEnabled().performClick()
        rule.onNodeWithContentDescription(text(other)).assertIsSelected()
        rule.runOnIdle {
            assertTrue(when (selectedFlow) {
                FlowId.CHALLENGE -> challengeService.cancelled
                FlowId.SOLUTION -> solutionService.cancelled
                FlowId.SOLUTION_H3 -> h3Service.cancelled
            })
        }
    }

    private class WaitingChallengeService : ChallengeService {
        var cancelled = false
        override suspend fun fetch(url: String, expectedPin: String): ChallengeResult =
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { cancelled = true }
            }
    }

    private class WaitingSolutionService : SolutionService {
        var cancelled = false
        override suspend fun fetch(url: String): SolutionResult = suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancelled = true }
        }
    }

    private class WaitingH3Service : H3Service {
        var cancelled = false
        override suspend fun fetch(url: String): H3Result = suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancelled = true }
        }
    }
}
