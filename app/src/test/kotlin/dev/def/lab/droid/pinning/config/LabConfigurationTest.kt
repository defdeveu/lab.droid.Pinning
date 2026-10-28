package dev.def.lab.droid.pinning.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabConfigurationTest {
    @Test
    fun acceptsHostedEndpoints() {
        assertTrue(LabConfiguration.isValidEndpoint("https://zsk.labs.def.dev/pinning/success", "https"))
        assertTrue(LabConfiguration.isValidEndpoint("http://zsk.labs.def.dev/pinning/success", "http"))
        assertTrue(LabConfiguration.isValidEndpoint("https://localhost:8443/pinning/success", "https"))
        assertTrue(LabConfiguration.isValidEndpoint("http://10.0.2.2:8080/pinning/success", "http"))
    }

    @Test
    fun rejectsTheWrongScheme() {
        assertFalse(LabConfiguration.isValidEndpoint("http://zsk.labs.def.dev/pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("https://zsk.labs.def.dev/pinning/success", "http"))
        assertFalse(LabConfiguration.isValidEndpoint("ftp://zsk.labs.def.dev/pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("file:/pinning/success", "https"))
    }

    @Test
    fun rejectsHostlessAddresses() {
        assertFalse(LabConfiguration.isValidEndpoint("https:/pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("https:///pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("https://", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("", "https"))
    }

    @Test
    fun rejectsOutOfRangePorts() {
        assertFalse(LabConfiguration.isValidEndpoint("https://zsk.labs.def.dev:0/pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("https://zsk.labs.def.dev:99999/pinning/success", "https"))
        assertFalse(LabConfiguration.isValidEndpoint("https://zsk.labs.def.dev:abc/pinning/success", "https"))
    }
}
