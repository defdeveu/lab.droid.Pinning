package defdev.labs.droid.pinning.flows.challenge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCheckerTest {
    private val classroomPin = "sha256/PlnlvxfcGEEUs/29JFwFdOb2lQKErvisb/6LqfXrlOw="

    @Test
    fun wellFormedAcceptsTheClassroomPin() {
        assertTrue(PinChecker.isWellFormed(classroomPin))
    }

    @Test
    fun wellFormedRejectsMalformedPins() {
        assertFalse(PinChecker.isWellFormed(""))
        assertFalse(PinChecker.isWellFormed("PlnlvxfcGEEUs/29JFwFdOb2lQKErvisb/6LqfXrlOw="))
        assertFalse(PinChecker.isWellFormed("sha256/not base64!"))
        assertFalse(PinChecker.isWellFormed("sha256/c2hvcnQ="))
    }
}
