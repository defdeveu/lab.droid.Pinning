package dev.def.lab.droid.pinning

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class PinningScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    @Test
    fun theScreenShowsThePinnedIdentitiesAndStartsReady() {
        composeRule
            .onNodeWithText(text(R.string.section_pinned_identities))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(text(R.string.status_ready))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(text(R.string.action_connect))
            .assertIsDisplayed()
    }
}
