package defdev.labdroid.pinning

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.pinning.config.LabConfiguration
import defdev.labdroid.pinning.crypto.PinChecker
import defdev.labdroid.pinning.error.LabError
import defdev.labdroid.pinning.service.PinningService
import defdev.labdroid.pinning.service.PlatformPinningService
import defdev.labdroid.pinning.store.FilePinStore
import defdev.labdroid.pinning.store.PinStore
import java.io.File

class AppContainer(context: Context) {
    private val service: PinningService = PlatformPinningService()
    private val store: PinStore = FilePinStore(File(context.filesDir, STATE_FILE))

    private val configurationError: LabError? = when {
        !LabConfiguration.isValidEndpoint(BuildConfig.LAB_PINNING_URL, "https") -> LabError.InvalidConfiguration
        !LabConfiguration.isValidEndpoint(BuildConfig.LAB_CLEARTEXT_URL, "http") -> LabError.InvalidConfiguration
        !PinChecker.isWellFormed(BuildConfig.LAB_LEAF_PIN) -> LabError.InvalidConfiguration
        else -> null
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PinningViewModel(
                service = service,
                store = store,
                pinnedUrl = BuildConfig.LAB_PINNING_URL,
                cleartextUrl = BuildConfig.LAB_CLEARTEXT_URL,
                pinnedPin = BuildConfig.LAB_LEAF_PIN,
                configurationError = configurationError,
            )
        }
    }

    private companion object {
        const val STATE_FILE = "pin-state"
    }
}
