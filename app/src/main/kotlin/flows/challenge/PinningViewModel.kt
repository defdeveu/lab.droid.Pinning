package defdev.labs.droid.pinning.flows.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import defdev.labs.droid.pinning.PinStatus
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinningUiState(
    val expectedPin: String,
    val leafPin: String = "",
    val result: String = "",
    val status: PinStatus = PinStatus.READY,
    val error: LabError? = null,
    val configurationError: LabError? = null,
    val operationId: Long = 0,
) {
    val isRunning: Boolean get() = status == PinStatus.CONNECTING

    val isConfigured: Boolean get() = configurationError == null
}

class PinningViewModel(
    private val service: PinningService,
    private val store: PinStore,
    private val pinnedUrl: String,
    private val cleartextUrl: String,
    private val pinnedPin: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    configurationError: LabError? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        PinningUiState(
            expectedPin = if (store.isBroken()) PinChecker.mangle(pinnedPin) else pinnedPin,
            configurationError = configurationError,
        ),
    )
    private var operation: Job? = null

    val uiState: StateFlow<PinningUiState> = mutableUiState.asStateFlow()

    fun connect() {
        fetch(pinnedUrl)
    }

    fun tryCleartext() {
        fetch(cleartextUrl)
    }

    fun breakPin() {
        if (mutableUiState.value.isRunning) return
        mutableUiState.update { it.copy(expectedPin = PinChecker.mangle(pinnedPin)) }
        store.setBroken(true)
    }

    fun restorePin() {
        if (mutableUiState.value.isRunning) return
        mutableUiState.update { it.copy(expectedPin = pinnedPin) }
        store.setBroken(false)
    }

    fun cancel() {
        if (!mutableUiState.value.isRunning) return
        mutableUiState.update {
            it.copy(operationId = it.operationId + 1, status = PinStatus.CANCELLED, error = null)
        }
        operation?.cancel()
    }

    private fun fetch(url: String) {
        val current = mutableUiState.value
        if (current.isRunning || !current.isConfigured) {
            return
        }
        val operationId = current.operationId + 1
        mutableUiState.update {
            it.copy(
                operationId = operationId,
                status = PinStatus.CONNECTING,
                error = null,
                result = "",
                leafPin = "",
            )
        }
        val expectedPin = current.expectedPin
        operation = viewModelScope.launch(dispatcher) {
            try {
                val fetched = service.fetch(url, expectedPin)
                publish(operationId) {
                    it.copy(
                        status = PinStatus.CONNECTED,
                        leafPin = fetched.leafPin,
                        result = describe(fetched),
                    )
                }
            } catch (failure: LabFailure) {
                publish(operationId) {
                    it.copy(
                        status = PinStatus.FAILED,
                        error = failure.reason,
                        leafPin = (failure.reason as? LabError.PinMismatch)?.actual.orEmpty(),
                    )
                }
            }
        }
    }

    private fun describe(fetched: FetchResult): String {
        val upgrade = fetched.upgradedFrom?.let { "upgraded from $it\n" }.orEmpty()
        return upgrade + "HTTP ${fetched.status}\n" + fetched.body
    }

    private fun publish(operationId: Long, transform: (PinningUiState) -> PinningUiState) {
        mutableUiState.update { state ->
            if (state.operationId == operationId) transform(state) else state
        }
    }
}
