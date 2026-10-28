package dev.def.lab.droid.pinning

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.def.lab.droid.pinning.config.LabConfiguration
import dev.def.lab.droid.pinning.crypto.PinChecker
import dev.def.lab.droid.pinning.error.LabError
import dev.def.lab.droid.pinning.service.PinningService
import dev.def.lab.droid.pinning.service.PlatformPinningService
import dev.def.lab.droid.pinning.store.FilePinStore
import dev.def.lab.droid.pinning.store.PinStore
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
