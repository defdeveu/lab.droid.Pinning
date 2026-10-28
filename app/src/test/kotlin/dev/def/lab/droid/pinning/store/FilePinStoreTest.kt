package dev.def.lab.droid.pinning.store

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FilePinStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun aFreshStoreIsNotBroken() {
        assertFalse(store().isBroken())
    }

    @Test
    fun theBrokenStateSurvivesANewStoreOverTheSameFile() {
        val file = File(folder.root, "pin-state")

        FilePinStore(file).setBroken(true)
        assertTrue(FilePinStore(file).isBroken())

        FilePinStore(file).setBroken(false)
        assertFalse(FilePinStore(file).isBroken())
    }

    private fun store(): FilePinStore = FilePinStore(File(folder.root, "pin-state"))
}
