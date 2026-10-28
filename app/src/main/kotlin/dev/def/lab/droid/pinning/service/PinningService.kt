package dev.def.lab.droid.pinning.service

import dev.def.lab.droid.pinning.crypto.PinChecker
import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.error.LabFailure
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.Socket
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager

data class FetchResult(
    val finalUrl: String,
    val status: Int,
    val body: String,
    val leafPin: String,
    val upgradedFrom: String?,
)

interface PinningService {
    fun fetch(url: String, expectedPin: String): FetchResult
}

class PlatformPinningService : PinningService {
    override fun fetch(url: String, expectedPin: String): FetchResult {
        val isCleartext = url.startsWith("http://")
        if (!isCleartext && !PinChecker.isWellFormed(expectedPin)) {
            throw LabFailure(LabError.PinNotConfigured)
        }
        val first = request(url, expectedPin.takeIf { !isCleartext })
        val location = first.location
        val result = if (isCleartext && first.status in 300..399 && location != null && location.startsWith("https://")) {
            request(location, expectedPin).toFetchResult(location, upgradedFrom = url)
        } else {
            first.toFetchResult(url)
        }
        if (result.status !in 200..299) {
            throw LabFailure(LabError.ServerStatus(result.status))
        }
        return result
    }

    private fun request(url: String, expectedPin: String?): Response {
        val recorder = PinRecorder()
        val connection = open(url, expectedPin, recorder)
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { it.readBytes().toString(Charsets.UTF_8) }.orEmpty()
            Response(
                status = status,
                body = body,
                location = connection.getHeaderField("Location"),
                leafPin = recorder.pin,
            )
        } catch (failure: IOException) {
            throw failure.toLabFailure()
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: String, expectedPin: String?, recorder: PinRecorder): HttpURLConnection {
        val parsed = try {
            URL(url)
        } catch (failure: MalformedURLException) {
            throw LabFailure(LabError.InvalidUrl)
        }
        if (parsed.protocol != "http" && parsed.protocol != "https") {
            throw LabFailure(LabError.InvalidUrl)
        }
        val connection = try {
            parsed.openConnection() as HttpURLConnection
        } catch (failure: IOException) {
            throw LabFailure(LabError.TransportFailed)
        }
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS
        connection.instanceFollowRedirects = false
        if (connection is HttpsURLConnection && expectedPin != null) {
            connection.sslSocketFactory = sslContext(PinningTrustManager(expectedPin, recorder)).socketFactory
        }
        return connection
    }

    private fun sslContext(trustManager: X509ExtendedTrustManager): SSLContext =
        SSLContext.getInstance("TLS").apply { init(null, arrayOf(trustManager), null) }

    private fun Response.toFetchResult(url: String, upgradedFrom: String? = null): FetchResult =
        FetchResult(
            finalUrl = url,
            status = status,
            body = body,
            leafPin = leafPin,
            upgradedFrom = upgradedFrom,
        )

    private fun IOException.toLabFailure(): LabFailure {
        val chain = generateSequence(this as Throwable?) { it.cause }.toList()
        chain.filterIsInstance<PinMismatchException>().firstOrNull()?.let { mismatch ->
            return LabFailure(LabError.PinMismatch(mismatch.expected, mismatch.actual))
        }
        val trustFailure = chain.any {
            it is CertificateException || it is SSLPeerUnverifiedException || it is SSLHandshakeException
        }
        return LabFailure(if (trustFailure) LabError.TrustFailed else LabError.TransportFailed)
    }

    private class PinRecorder {
        var pin: String = ""
    }

    private class PinMismatchException(val expected: String, val actual: String) :
        CertificateException("pin mismatch")

    private class PinningTrustManager(
        private val expectedPin: String,
        private val recorder: PinRecorder,
    ) : X509ExtendedTrustManager() {
        private val delegate: X509ExtendedTrustManager = platformTrustManager()

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            delegate.checkClientTrusted(chain, authType)
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket) {
            delegate.checkClientTrusted(chain, authType, socket)
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine) {
            delegate.checkClientTrusted(chain, authType, engine)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            delegate.checkServerTrusted(chain, authType)
            checkPin(chain)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket) {
            delegate.checkServerTrusted(chain, authType, socket)
            checkPin(chain)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine) {
            delegate.checkServerTrusted(chain, authType, engine)
            checkPin(chain)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers

        private fun checkPin(chain: Array<X509Certificate>) {
            if (chain.isEmpty()) {
                return
            }
            val actual = PinChecker.pin(chain[0])
            recorder.pin = actual
            if (!PinChecker.matches(expectedPin, chain[0])) {
                throw PinMismatchException(expectedPin, actual)
            }
        }
    }

    private data class Response(
        val status: Int,
        val body: String,
        val location: String?,
        val leafPin: String,
    )

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
    }
}

private fun platformTrustManager(): X509ExtendedTrustManager {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    factory.init(null as KeyStore?)
    return factory.trustManagers.filterIsInstance<X509ExtendedTrustManager>().first()
}
