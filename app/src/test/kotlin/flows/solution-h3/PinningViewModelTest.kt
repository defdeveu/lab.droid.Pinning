package defdev.labs.droid.pinning.flows.solutionh3

import defdev.labs.droid.pinning.PinStatus
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
import defdev.labs.droid.pinning.flows.solution.PinnedSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun theResponseAndProtocolArePublished() = runTest(dispatcher) {
        val service = StubService()
        val model = model(service)
        model.connect()
        advanceUntilIdle()
        assertEquals(PinStatus.CONNECTED, model.uiState.value.status)
        assertTrue(model.uiState.value.result.contains("Protocol: h3"))
    }

    @Test fun aFailureIsReported() = runTest(dispatcher) {
        val model = model(StubService(failure = LabError.ServerStatus(505)))
        model.connect()
        advanceUntilIdle()
        assertEquals(PinStatus.FAILED, model.uiState.value.status)
        assertEquals(LabError.ServerStatus(505), model.uiState.value.error)
    }

    @Test fun anUnavailableCaseDoesNotStartARequest() = runTest(dispatcher) {
        val service = StubService()
        val model = model(service, false)
        model.connect()
        advanceUntilIdle()
        assertEquals(0, service.calls)
        assertEquals(PinStatus.READY, model.uiState.value.status)
    }

    @Test fun cancellationStopsTheOperationAndAllowsAnotherRequest() = runTest(dispatcher) {
        val service = StubService(waiting = true)
        val model = model(service)
        model.connect()
        advanceUntilIdle()
        assertTrue(model.uiState.value.isRunning)
        model.cancel()
        advanceUntilIdle()
        assertTrue(service.cancelled)
        assertFalse(model.uiState.value.isRunning)
        assertEquals(PinStatus.CANCELLED, model.uiState.value.status)
        model.connect()
        advanceUntilIdle()
        assertEquals(2, service.calls)
        model.cancel()
    }

    private fun model(service: PinningService, available: Boolean = true) = PinningViewModel(
        service, "https://lab.example/pinning/h3", PinnedSet("lab.example", listOf("identity")), available, dispatcher,
    )

    private class StubService(val waiting: Boolean = false, val failure: LabError? = null) : PinningService {
        var calls = 0
        var cancelled = false
        override suspend fun fetch(url: String): FetchResult {
            calls++
            if (waiting) return suspendCancellableCoroutine { it.invokeOnCancellation { cancelled = true } }
            if (failure != null) throw LabFailure(failure)
            return FetchResult(200, "Connection succeeded.", "h3")
        }
    }
}
