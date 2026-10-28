package dev.def.lab.droid.pinning

import androidx.lifecycle.viewModelScope
import dev.def.lab.droid.pinning.config.PinnedSet
import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.error.LabFailure
import dev.def.lab.droid.pinning.service.FetchResult
import dev.def.lab.droid.pinning.service.PinningService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinningViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val pinnedSet = PinnedSet(
        host = "zsk.labs.def.dev",
        digests = listOf("first-digest", "second-digest"),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun thePinnedSetIsExposed() {
        val state = viewModel().uiState.value

        assertEquals("zsk.labs.def.dev", state.pinnedHost)
        assertEquals(pinnedSet.digests, state.pinnedDigests)
    }

    @Test
    fun connectPublishesTheResponse() = runTest(dispatcher) {
        val service = StubService(FetchResult(200, "Connection succeeded."))
        val viewModel = viewModel(service)

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(ConnectStatus.CONNECTED, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.result.contains("HTTP 200"))
        assertTrue(viewModel.uiState.value.result.contains("Connection succeeded."))
    }

    @Test
    fun aTrustFailureIsReported() = runTest(dispatcher) {
        val viewModel = viewModel(StubService(failure = LabError.TrustFailed))

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(ConnectStatus.FAILED, viewModel.uiState.value.status)
        assertEquals(LabError.TrustFailed, viewModel.uiState.value.error)
    }

    @Test
    fun aStatusFailureIsReported() = runTest(dispatcher) {
        val viewModel = viewModel(StubService(failure = LabError.ServerStatus(503)))

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(ConnectStatus.FAILED, viewModel.uiState.value.status)
        assertEquals(LabError.ServerStatus(503), viewModel.uiState.value.error)
    }

    @Test
    fun anInvalidConfigurationDisablesConnecting() = runTest(dispatcher) {
        val service = StubService()
        val viewModel = viewModel(service = service, configurationError = LabError.InvalidConfiguration)

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(0, service.calls)
        assertFalse(viewModel.uiState.value.isConfigured)
    }

    @Test
    fun aSecondConnectWhileRunningIsIgnored() = runTest(dispatcher) {
        val service = StubService()
        val viewModel = viewModel(service)

        viewModel.connect()
        viewModel.connect()
        advanceUntilIdle()

        assertEquals(1, service.calls)
    }

    @Test
    fun cancellingTheScopeCancelsAnInFlightRequest() = runTest(dispatcher) {
        val service = SuspendingService()
        val viewModel = viewModel(service)

        viewModel.connect()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()

        assertTrue(service.cancelled)
    }

    private fun viewModel(
        service: PinningService = StubService(),
        configurationError: LabError? = null,
    ) = PinningViewModel(
        service = service,
        pinnedUrl = "https://lab/success",
        pinnedSet = pinnedSet,
        dispatcher = dispatcher,
        configurationError = configurationError,
    )

    private class StubService(
        private val result: FetchResult = FetchResult(200, "ok"),
        private val failure: LabError? = null,
    ) : PinningService {
        var calls = 0
            private set

        override suspend fun fetch(url: String): FetchResult {
            calls++
            failure?.let { throw LabFailure(it) }
            return result
        }
    }

    private class SuspendingService : PinningService {
        var cancelled = false

        override suspend fun fetch(url: String): FetchResult = suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancelled = true }
        }
    }
}
