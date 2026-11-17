package defdev.labs.droid.pinning.flows.challenge

import defdev.labs.droid.pinning.PinStatus
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
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
    private val classroomPin = "sha256/PlnlvxfcGEEUs/29JFwFdOb2lQKErvisb/6LqfXrlOw="

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun connectPublishesTheServedIdentity() = runTest(dispatcher) {
        val service = StubService(
            FetchResult("https://lab/success", 200, "Connection succeeded.", "sha256/served", null),
        )
        val viewModel = viewModel(service)

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(PinStatus.CONNECTED, viewModel.uiState.value.status)
        assertEquals("sha256/served", viewModel.uiState.value.leafPin)
        assertTrue(viewModel.uiState.value.result.contains("HTTP 200"))
        assertEquals(classroomPin, service.lastExpectedPin)
    }

    @Test
    fun aPinMismatchIsReportedAndKeepsTheServedIdentity() = runTest(dispatcher) {
        val viewModel = viewModel(
            service = StubService(failure = LabError.PinMismatch(classroomPin, "sha256/served")),
        )

        viewModel.connect()
        advanceUntilIdle()

        assertEquals(PinStatus.FAILED, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.error is LabError.PinMismatch)
        assertEquals("sha256/served", viewModel.uiState.value.leafPin)
        assertEquals("", viewModel.uiState.value.result)
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
    fun anInvalidConfigurationDisablesConnecting() = runTest(dispatcher) {
        val service = StubService()
        val viewModel = viewModel(service = service, configurationError = LabError.InvalidConfiguration)

        viewModel.connect()
        viewModel.tryCleartext()
        advanceUntilIdle()

        assertEquals(0, service.calls)
        assertFalse(viewModel.uiState.value.isConfigured)
    }

    private fun viewModel(
        service: PinningService = StubService(),
        configurationError: LabError? = null,
    ) = PinningViewModel(
        service = service,
        pinnedUrl = "https://lab/success",
        cleartextUrl = "http://lab/success",
        pinnedPin = classroomPin,
        dispatcher = dispatcher,
        configurationError = configurationError,
    )

    @Test
    fun cancellingTheRequestKeepsTheExpectedIdentityAndAllowsRetry() = runTest(dispatcher) {
        val service = WaitingService()
        val viewModel = viewModel(service)
        val expected = viewModel.uiState.value.expectedPin
        viewModel.connect()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isRunning)

        viewModel.cancel()
        advanceUntilIdle()

        assertTrue(service.cancelled)
        assertEquals(PinStatus.CANCELLED, viewModel.uiState.value.status)
        assertEquals(expected, viewModel.uiState.value.expectedPin)
        viewModel.connect()
        advanceUntilIdle()
        assertEquals(2, service.calls)
        viewModel.cancel()
        advanceUntilIdle()
    }

    private class WaitingService : PinningService {
        var cancelled = false
        var calls = 0
        override suspend fun fetch(url: String, expectedPin: String): FetchResult =
            suspendCancellableCoroutine { continuation ->
                calls++
                continuation.invokeOnCancellation { cancelled = true }
            }
    }

    private class StubService(
        private val result: FetchResult = FetchResult("https://lab/success", 200, "ok", "sha256/served", null),
        private val failure: LabError? = null,
    ) : PinningService {
        var calls = 0
            private set
        var lastExpectedPin = ""
            private set

        override suspend fun fetch(url: String, expectedPin: String): FetchResult {
            calls++
            lastExpectedPin = expectedPin
            failure?.let { throw LabFailure(it) }
            return result
        }
    }
}
