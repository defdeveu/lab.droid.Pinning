package defdev.labs.droid.pinning.flows.challenge

import java.security.MessageDigest
import java.security.cert.X509Certificate
import kotlin.io.encoding.Base64

object PinChecker {
    const val PREFIX = "sha256/"

    private const val DIGEST_ALGORITHM = "SHA-256"

    fun pin(certificate: X509Certificate): String =
        PREFIX + Base64.encode(digest(certificate.publicKey.encoded))

    fun matches(expected: String, certificate: X509Certificate): Boolean =
        expected == pin(certificate)

    fun isWellFormed(pin: String): Boolean {
        if (!pin.startsWith(PREFIX)) {
            return false
        }
        val payload = pin.removePrefix(PREFIX)
        if (payload.isEmpty()) {
            return false
        }
        return try {
            Base64.decode(payload).size == 32
        } catch (failure: IllegalArgumentException) {
            false
        }
    }

    private fun digest(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance(DIGEST_ALGORITHM).digest(bytes)
}
