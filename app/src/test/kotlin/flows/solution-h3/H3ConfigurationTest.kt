package defdev.labs.droid.pinning.flows.solutionh3

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class H3ConfigurationTest {
    private val hosted = "https://lab.example/pinning/h3"

    @Test fun theSuppliedEndpointIsAvailableOnSupportedVersions() {
        assertTrue(H3Configuration.isAvailable(34, hosted, hosted))
    }

    @Test fun earlierVersionsKeepTheCaseUnavailable() {
        assertFalse(H3Configuration.isAvailable(33, hosted, hosted))
    }

    @Test fun anotherOriginKeepsTheCaseUnavailable() {
        assertFalse(H3Configuration.isAvailable(37, "https://localhost/pinning/h3", hosted))
    }

    @Test fun anInvalidEndpointIsUnavailable() {
        assertFalse(H3Configuration.isAvailable(37, "https:/pinning/h3", hosted))
        assertFalse(H3Configuration.isAvailable(37, "http://lab.example/pinning/h3", hosted))
        assertFalse(H3Configuration.isAvailable(37, "https://lab.example/pinning/success", hosted))
        assertFalse(H3Configuration.isAvailable(37, "https://lab.example:70000/pinning/h3", hosted))
        assertTrue(H3Configuration.isAvailable(37, "https://lab.example:443/pinning/h3", hosted))
    }

    @Test fun configuredDigestsAreDecoded() {
        val pins = H3Configuration.decodePins(listOf("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))
        assertEquals(1, pins?.size)
        assertEquals(32, pins?.single()?.size)
    }

    @Test fun anEmptySetIsUnavailable() {
        assertNull(H3Configuration.decodePins(emptyList()))
    }

    @Test fun malformedDigestsAreUnavailable() {
        assertNull(H3Configuration.decodePins(listOf("not a digest")))
        assertNull(H3Configuration.decodePins(listOf("YQ==")))
    }
}
