package dev.def.lab.droid.pinning.config

import android.util.Xml
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PinnedSetReaderTest {
    @Test
    fun readsTheDigestsForTheHost() {
        val set = read(networkSecurityConfig(), "zsk.labs.def.dev")

        assertEquals("zsk.labs.def.dev", set?.host)
        assertEquals(listOf("first-digest", "second-digest"), set?.digests)
    }

    @Test
    fun rejectsAnUnknownHost() {
        assertNull(read(networkSecurityConfig(), "other.example"))
    }

    @Test
    fun rejectsAnEmptyPinSet() {
        val withoutPins = networkSecurityConfig()
            .replace("""<pin digest="SHA-256">first-digest</pin>""", "")
            .replace("""<pin digest="SHA-256">second-digest</pin>""", "")

        assertNull(read(withoutPins, "zsk.labs.def.dev"))
    }

    private fun networkSecurityConfig(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <network-security-config>
            <domain-config>
                <domain includeSubdomains="false">zsk.labs.def.dev</domain>
                <pin-set>
                    <pin digest="SHA-256">first-digest</pin>
                    <pin digest="SHA-256">second-digest</pin>
                </pin-set>
            </domain-config>
        </network-security-config>
    """.trimIndent()

    private fun read(xml: String, host: String): PinnedSet? {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))
        return PinnedSetReader.read(parser, host)
    }
}
