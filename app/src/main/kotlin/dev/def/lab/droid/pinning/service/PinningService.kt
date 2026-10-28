package dev.def.lab.droid.pinning.service

import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.error.LabFailure
import java.io.IOException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class FetchResult(
    val status: Int,
    val body: String,
)

interface PinningService {
    suspend fun fetch(url: String): FetchResult
}

class OkHttpPinningService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build(),
) : PinningService {
    override suspend fun fetch(url: String): FetchResult {
        val request = try {
            Request.Builder().url(url).build()
        } catch (failure: IllegalArgumentException) {
            throw LabFailure(LabError.InvalidUrl)
        }
        val received = try {
            client.newCall(request).awaitExchange()
        } catch (failure: IOException) {
            throw failure.toLabFailure()
        }
        if (received.status !in 200..299) {
            throw LabFailure(LabError.ServerStatus(received.status))
        }
        return FetchResult(status = received.status, body = received.body)
    }

    private suspend fun Call.awaitExchange(): Received = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val received = try {
                        response.use {
                            Received(status = it.code, body = it.body.string())
                        }
                    } catch (failure: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(failure)
                        }
                        return
                    }
                    if (continuation.isActive) {
                        continuation.resume(received)
                    }
                }
            },
        )
    }

    private data class Received(val status: Int, val body: String)

    private fun IOException.toLabFailure(): LabFailure {
        val chain = generateSequence(this as Throwable?) { it.cause }.toList()
        val trustFailure = chain.any {
            it is CertificateException || it is SSLPeerUnverifiedException || it is SSLHandshakeException
        }
        return LabFailure(if (trustFailure) LabError.TrustFailed else LabError.TransportFailed)
    }

    private companion object {
        const val TIMEOUT_SECONDS = 10L
    }
}
