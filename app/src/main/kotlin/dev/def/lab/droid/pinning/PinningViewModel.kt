package dev.def.lab.droid.pinning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.def.lab.droid.pinning.config.PinnedSet
import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.error.LabFailure
import dev.def.lab.droid.pinning.service.PinningService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConnectStatus { READY, CONNECTING, CONNECTED, FAILED }

data class PinningUiState(
    val pinnedHost: String,
    val pinnedDigests: List<String>,
    val result: String = "",
    val status: ConnectStatus = ConnectStatus.READY,
    val error: LabError? = null,
    val configurationError: LabError? = null,
    val operationId: Long = 0,
) {
    val isRunning: Boolean get() = status == ConnectStatus.CONNECTING

    val isConfigured: Boolean get() = configurationError == null
}

class PinningViewModel(
    private val service: PinningService,
    private val pinnedUrl: String,
    pinnedSet: PinnedSet?,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    configurationError: LabError? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        PinningUiState(
            pinnedHost = pinnedSet?.host.orEmpty(),
            pinnedDigests = pinnedSet?.digests.orEmpty(),
            configurationError = configurationError,
        ),
    )
    private var operation: Job? = null

    val uiState: StateFlow<PinningUiState> = mutableUiState.asStateFlow()

    fun connect() {
        val current = mutableUiState.value
        if (current.isRunning || !current.isConfigured) {
            return
        }
        val operationId = current.operationId + 1
        mutableUiState.update {
            it.copy(
                operationId = operationId,
                status = ConnectStatus.CONNECTING,
                error = null,
                result = "",
            )
        }
        operation = viewModelScope.launch(dispatcher) {
            try {
                val fetched = service.fetch(pinnedUrl)
                publish(operationId) {
                    it.copy(
                        status = ConnectStatus.CONNECTED,
                        result = "HTTP ${fetched.status}\n${fetched.body}",
                    )
                }
            } catch (failure: LabFailure) {
                publish(operationId) {
                    it.copy(status = ConnectStatus.FAILED, error = failure.reason)
                }
            }
        }
    }

    private fun publish(operationId: Long, transform: (PinningUiState) -> PinningUiState) {
        mutableUiState.update { state ->
            if (state.operationId == operationId) transform(state) else state
        }
    }
}
