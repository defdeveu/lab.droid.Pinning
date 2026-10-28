package dev.def.lab.droid.pinning

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dev.def.lab.droid.pinning.crypto.PinChecker
import org.junit.Rule
import org.junit.Test

class PinningScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

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
