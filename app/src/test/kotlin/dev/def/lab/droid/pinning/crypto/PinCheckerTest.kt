package dev.def.lab.droid.pinning.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCheckerTest {
    private val classroomPin = "sha256/PlnlvxfcGEEUs/29JFwFdOb2lQKErvisb/6LqfXrlOw="

    @Test
    fun mangleChangesExactlyOneCharacter() {
        val mangled = PinChecker.mangle(classroomPin)

        assertEquals(classroomPin.length, mangled.length)
        assertEquals(1, classroomPin.zip(mangled).count { (original, changed) -> original != changed })
        assertTrue(mangled.startsWith(PinChecker.PREFIX))
        assertNotEquals(classroomPin, mangled)
    }

    @Test
    fun mangleIsDeterministic() {
        assertEquals(PinChecker.mangle(classroomPin), PinChecker.mangle(classroomPin))
    }

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
