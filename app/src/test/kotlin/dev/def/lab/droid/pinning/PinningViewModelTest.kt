package dev.def.lab.droid.pinning

import dev.def.lab.droid.pinning.crypto.PinChecker
import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.error.LabFailure
import dev.def.lab.droid.pinning.service.FetchResult
import dev.def.lab.droid.pinning.service.PinningService
import dev.def.lab.droid.pinning.store.PinStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
    fun breakThenRestoreChangesTheExpectedIdentity() = runTest(dispatcher) {
        val store = FakePinStore()
        val viewModel = viewModel(store = store)

        viewModel.breakPin()
        assertNotEquals(classroomPin, viewModel.uiState.value.expectedPin)
        assertTrue(store.brokenValue)

        viewModel.restorePin()
        assertEquals(classroomPin, viewModel.uiState.value.expectedPin)
        assertFalse(store.brokenValue)
    }

    @Test
    fun repeatedBreakKeepsTheSameBrokenPin() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.breakPin()
        val first = viewModel.uiState.value.expectedPin
        viewModel.breakPin()
        val second = viewModel.uiState.value.expectedPin

        assertEquals(first, second)
        assertNotEquals(classroomPin, second)
        assertEquals(PinChecker.mangle(classroomPin), second)
    }

    @Test
    fun aStoredBrokenStateIsRestoredOnLaunch() = runTest(dispatcher) {
        val viewModel = viewModel(store = FakePinStore(brokenValue = true))

        assertEquals(PinChecker.mangle(classroomPin), viewModel.uiState.value.expectedPin)
    }

    @Test
    fun theBrokenIdentityIsTheOneSentToTheServer() = runTest(dispatcher) {
        val service = StubService()
        val viewModel = viewModel(service)

        viewModel.breakPin()
        viewModel.connect()
        advanceUntilIdle()

        assertEquals(viewModel.uiState.value.expectedPin, service.lastExpectedPin)
        assertNotEquals(classroomPin, service.lastExpectedPin)
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
        store: PinStore = FakePinStore(),
        configurationError: LabError? = null,
    ) = PinningViewModel(
        service = service,
        store = store,
        pinnedUrl = "https://lab/success",
        cleartextUrl = "http://lab/success",
        pinnedPin = classroomPin,
        dispatcher = dispatcher,
        configurationError = configurationError,
    )

    private class FakePinStore(var brokenValue: Boolean = false) : PinStore {
        override fun isBroken(): Boolean = brokenValue

        override fun setBroken(broken: Boolean) {
            brokenValue = broken
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

        override fun fetch(url: String, expectedPin: String): FetchResult {
            calls++
            lastExpectedPin = expectedPin
            failure?.let { throw LabFailure(it) }
            return result
        }
    }
}
