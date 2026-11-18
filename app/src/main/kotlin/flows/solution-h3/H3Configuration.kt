package defdev.labs.droid.pinning.flows.solutionh3

import java.net.URI
import java.util.Base64

object H3Configuration {
    fun isAvailable(sdk: Int, url: String, hostedUrl: String): Boolean {
        if (sdk < 34) return false
        return try {
            val selected = URI(url)
            val hosted = URI(hostedUrl)
            val selectedPort = if (selected.port == -1) 443 else selected.port
            val hostedPort = if (hosted.port == -1) 443 else hosted.port
            selected.scheme == "https" && hosted.scheme == "https" && !selected.host.isNullOrEmpty() &&
                selected.host.equals(hosted.host, ignoreCase = true) &&
                selectedPort in 1..65535 && selectedPort == hostedPort && selected.path == "/pinning/h3" &&
                selected.userInfo == null && selected.query == null && selected.fragment == null
        } catch (failure: java.net.URISyntaxException) {
            false
        }
    }

    fun decodePins(digests: List<String>): Set<ByteArray>? {
        if (digests.isEmpty()) return null
        return try {
            val pins = digests.map { Base64.getDecoder().decode(it) }
            if (pins.any { it.size != 32 }) null else pins.toSet()
        } catch (failure: IllegalArgumentException) {
            null
        }
    }
}
