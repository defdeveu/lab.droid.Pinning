package defdev.labs.droid.pinning.flows.solutionh3

import android.content.Context
import android.net.http.HttpEngine
import android.net.http.HttpException
import android.net.http.UrlRequest
import android.net.http.UrlResponseInfo
import androidx.annotation.RequiresApi
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

data class FetchResult(val status: Int, val body: String, val protocol: String)

interface PinningService {
    suspend fun fetch(url: String): FetchResult
}

@RequiresApi(34)
class HttpEnginePinningService(context: Context, private val authorityPins: Set<ByteArray>) : PinningService {
    private val context = context.applicationContext

    override suspend fun fetch(url: String): FetchResult = try {
        withTimeout(15_000) { exchange(url) }
    } catch (failure: TimeoutCancellationException) {
        throw LabFailure(LabError.TransportFailed)
    }

    private suspend fun exchange(url: String): FetchResult = suspendCancellableCoroutine { continuation ->
        val endpoint = URI(url)
        val port = endpoint.port.takeIf { it != -1 } ?: 443
        val executor = Executors.newSingleThreadExecutor()
        val engine = try {
            HttpEngine.Builder(context)
                .setEnableQuic(true)
                .addQuicHint(endpoint.host, port, port)
                .addPublicKeyPins(endpoint.host, authorityPins, false, Instant.ofEpochMilli(Long.MAX_VALUE))
                .setEnablePublicKeyPinningBypassForLocalTrustAnchors(false)
                .build()
        } catch (failure: RuntimeException) {
            executor.shutdown()
            continuation.resumeWithException(LabFailure(LabError.TransportFailed))
            return@suspendCancellableCoroutine
        }
        val body = ByteArrayOutputStream()
        var responseFailure: LabFailure? = null

        fun finish(result: Result<FetchResult>) {
            try {
                engine.shutdown()
            } finally {
                executor.shutdown()
            }
            if (continuation.isActive) continuation.resumeWith(result)
        }

        val callback = object : UrlRequest.Callback {
            override fun onRedirectReceived(request: UrlRequest, info: UrlResponseInfo, location: String) {
                responseFailure = LabFailure(LabError.InvalidUrl)
                request.cancel()
            }

            override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                request.read(ByteBuffer.allocateDirect(4096))
            }

            override fun onReadCompleted(request: UrlRequest, info: UrlResponseInfo, buffer: ByteBuffer) {
                buffer.flip()
                if (body.size() + buffer.remaining() > 65_536) {
                    responseFailure = LabFailure(LabError.TransportFailed)
                    request.cancel()
                    return
                }
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                body.write(bytes)
                buffer.clear()
                request.read(buffer)
            }

            override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
                val result = when {
                    info.httpStatusCode !in 200..299 -> Result.failure(LabFailure(LabError.ServerStatus(info.httpStatusCode)))
                    info.negotiatedProtocol != "h3" && !info.negotiatedProtocol.startsWith("h3-") ->
                        Result.failure(LabFailure(LabError.Http3Required))
                    else -> Result.success(FetchResult(info.httpStatusCode, body.toString(Charsets.UTF_8.name()), info.negotiatedProtocol))
                }
                finish(result)
            }

            override fun onFailed(request: UrlRequest, info: UrlResponseInfo?, failure: HttpException) {
                finish(Result.failure(LabFailure(LabError.TransportFailed)))
            }

            override fun onCanceled(request: UrlRequest, info: UrlResponseInfo?) {
                finish(Result.failure(responseFailure ?: CancellationException()))
            }
        }
        try {
            val request = engine.newUrlRequestBuilder(url, executor, callback).setCacheDisabled(true).build()
            request.start()
            continuation.invokeOnCancellation { request.cancel() }
        } catch (failure: RuntimeException) {
            finish(Result.failure(LabFailure(LabError.TransportFailed)))
        }
    }
}
