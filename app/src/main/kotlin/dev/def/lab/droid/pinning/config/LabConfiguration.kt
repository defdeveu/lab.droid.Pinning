package dev.def.lab.droid.pinning.config

import java.net.MalformedURLException
import java.net.URL

object LabConfiguration {
    fun isValidEndpoint(url: String, scheme: String): Boolean {
        val parsed = try {
            URL(url)
        } catch (failure: MalformedURLException) {
            return false
        }
        if (parsed.protocol != scheme) {
            return false
        }
        if (parsed.host.isNullOrEmpty()) {
            return false
        }
        return parsed.port == -1 || parsed.port in 1..MAX_PORT
    }

    private const val MAX_PORT = 65535
}
