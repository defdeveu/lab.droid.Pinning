package dev.def.lab.droid.pinning.config

import androidx.test.platform.app.InstrumentationRegistry
import dev.def.lab.droid.pinning.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommittedPinnedSetTest {
    @Test
    fun theCommittedConfigPinsTheHostedAuthoritySet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val set = requireNotNull(
            context.resources.getXml(R.xml.network_security_config).use { parser ->
                PinnedSetReader.read(parser, "zsk.labs.def.dev")
            },
        )

        assertEquals("zsk.labs.def.dev", set.host)
        assertEquals(3, set.digests.size)
        assertTrue(set.digests.all { it.isNotBlank() })
    }
}
