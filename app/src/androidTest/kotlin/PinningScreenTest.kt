package defdev.labs.droid.pinning

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import defdev.labs.droid.pinning.flows.challenge.PinChecker
import org.junit.Rule
import org.junit.Test

class PinningScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    @Test
    fun theSelectedCaseSurvivesRecreation() {
        composeRule.onNodeWithContentDescription(text(R.string.flow_solution_title)).performClick()
        composeRule.runOnUiThread {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<MainActivity>().single().recreate()
        }
        composeRule.onNodeWithContentDescription(text(R.string.flow_solution_title)).assertIsSelected()
    }

    @Test
    fun theScreenShowsTheExpectedIdentityAndStartsReady() {
        composeRule
            .onNodeWithText(text(R.string.section_expected_pin))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(text(R.string.status_ready))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(text(R.string.action_connect))
            .assertIsDisplayed()
    }

    @Test
    fun breakAndRestoreUpdateTheExpectedIdentity() {
        composeRule
            .onNodeWithText(text(R.string.action_break_pin))
            .performClick()
        composeRule
            .onNodeWithText(PinChecker.mangle(BuildConfig.LAB_LEAF_PIN))
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(text(R.string.action_restore_pin))
            .performClick()
        composeRule
            .onNodeWithText(BuildConfig.LAB_LEAF_PIN)
            .assertIsDisplayed()
    }
}
