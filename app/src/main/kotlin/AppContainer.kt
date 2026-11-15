package defdev.labdroid.pinning

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labdroid.pinning.config.LabConfiguration
import defdev.labdroid.pinning.config.PinnedSet
import defdev.labdroid.pinning.config.PinnedSetReader
import defdev.labdroid.pinning.error.LabError
import defdev.labdroid.pinning.service.OkHttpPinningService
import defdev.labdroid.pinning.service.PinningService
import java.net.MalformedURLException
import java.net.URL

class AppContainer(context: Context) {
    private val service: PinningService = OkHttpPinningService()
    private val pinnedUrl: String = BuildConfig.LAB_PINNING_URL
    private val pinnedHost: String = try {
        URL(pinnedUrl).host.orEmpty()
    } catch (failure: MalformedURLException) {
        ""
    }
    private val pinnedSet: PinnedSet? = pinnedHost
        .takeIf { it.isNotEmpty() }
        ?.let { host ->
            try {
                context.resources.getXml(R.xml.network_security_config).use { parser ->
                    PinnedSetReader.read(parser, host)
                }
            } catch (failure: RuntimeException) {
                null
            }
        }

    private val configurationError: LabError? = when {
        !LabConfiguration.isValidEndpoint(pinnedUrl, "https") -> LabError.InvalidConfiguration
        pinnedSet == null -> LabError.InvalidConfiguration
        else -> null
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PinningViewModel(
                service = service,
                pinnedUrl = pinnedUrl,
                pinnedSet = pinnedSet,
                configurationError = configurationError,
            )
        }
    }
}
