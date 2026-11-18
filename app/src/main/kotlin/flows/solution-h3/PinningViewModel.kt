package defdev.labs.droid.pinning.flows.solutionh3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import defdev.labs.droid.pinning.PinStatus
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
import defdev.labs.droid.pinning.flows.solution.PinnedSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinningUiState(
    val pinnedHost: String,
    val pinnedDigests: List<String>,
    val result: String = "",
    val status: PinStatus = PinStatus.READY,
    val error: LabError? = null,
    val operationId: Long = 0,
) {
    val isRunning: Boolean get() = status == PinStatus.CONNECTING
}

class PinningViewModel(
    private val service: PinningService,
    private val pinnedUrl: String,
    pinnedSet: PinnedSet?,
    val isAvailable: Boolean,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        PinningUiState(pinnedSet?.host.orEmpty(), pinnedSet?.digests.orEmpty()),
    )
    private var operation: Job? = null
    val uiState: StateFlow<PinningUiState> = mutableUiState.asStateFlow()

    fun connect() {
        val current = mutableUiState.value
        if (!isAvailable || current.isRunning) return
        val operationId = current.operationId + 1
        mutableUiState.update { it.copy(operationId = operationId, status = PinStatus.CONNECTING, error = null, result = "") }
        operation = viewModelScope.launch(dispatcher) {
            try {
                val response = service.fetch(pinnedUrl)
                publish(operationId) {
                    it.copy(status = PinStatus.CONNECTED,
                        result = "HTTP ${response.status}\nProtocol: ${response.protocol}\n${response.body}")
                }
            } catch (failure: LabFailure) {
                publish(operationId) { it.copy(status = PinStatus.FAILED, error = failure.reason) }
            }
        }
    }

    fun cancel() {
        if (!mutableUiState.value.isRunning) return
        mutableUiState.update { it.copy(operationId = it.operationId + 1, status = PinStatus.CANCELLED, error = null) }
        operation?.cancel()
    }

    private fun publish(operationId: Long, transform: (PinningUiState) -> PinningUiState) {
        mutableUiState.update { state -> if (state.operationId == operationId) transform(state) else state }
    }
}
