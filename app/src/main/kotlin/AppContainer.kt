package defdev.labs.droid.pinning

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import defdev.labs.droid.pinning.config.LabConfiguration
import defdev.labs.droid.pinning.error.LabError
import defdev.labs.droid.pinning.error.LabFailure
import defdev.labs.droid.pinning.flows.solutionh3.H3Configuration
import defdev.labs.droid.pinning.flows.solutionh3.HttpEnginePinningService
import defdev.labs.droid.pinning.flows.solutionh3.PinningService as H3Service
import defdev.labs.droid.pinning.flows.solutionh3.PinningViewModel as H3ViewModel
import defdev.labs.droid.pinning.flows.challenge.PinChecker
import defdev.labs.droid.pinning.flows.challenge.PlatformPinningService
import defdev.labs.droid.pinning.flows.solution.OkHttpPinningService
import defdev.labs.droid.pinning.flows.solution.PinnedSet
import defdev.labs.droid.pinning.flows.solution.PinnedSetReader
import defdev.labs.droid.pinning.flows.challenge.PinningViewModel as ChallengeViewModel
import defdev.labs.droid.pinning.flows.solution.PinningViewModel as SolutionViewModel
import java.net.MalformedURLException
import java.net.URL

class AppContainer(context: Context) {
    private val challengeError: LabError? = when {
        !LabConfiguration.isValidEndpoint(BuildConfig.LAB_PINNING_URL, "https") -> LabError.InvalidConfiguration
        !LabConfiguration.isValidEndpoint(BuildConfig.LAB_CLEARTEXT_URL, "http") -> LabError.InvalidConfiguration
        !PinChecker.isWellFormed(BuildConfig.LAB_LEAF_PIN) -> LabError.InvalidConfiguration
        else -> null
    }

    val challengeFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ChallengeViewModel(
                service = PlatformPinningService(),
                pinnedUrl = BuildConfig.LAB_PINNING_URL,
                cleartextUrl = BuildConfig.LAB_CLEARTEXT_URL,
                pinnedPin = BuildConfig.LAB_LEAF_PIN,
                configurationError = challengeError,
            )
        }
    }

    private val solutionUrl = BuildConfig.LAB_SOLUTION_URL
    private val solutionHost = try {
        URL(solutionUrl).host.orEmpty()
    } catch (failure: MalformedURLException) {
        ""
    }
    private val pinnedSet: PinnedSet? = solutionHost.takeIf { it.isNotEmpty() }?.let { host ->
        try {
            context.resources.getXml(R.xml.network_security_config).use { parser ->
                PinnedSetReader.read(parser, host)
            }
        } catch (failure: RuntimeException) {
            null
        }
    }
    private val solutionError = when {
        !LabConfiguration.isValidEndpoint(solutionUrl, "https") -> LabError.InvalidConfiguration
        pinnedSet == null -> LabError.InvalidConfiguration
        else -> null
    }

    val solutionFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SolutionViewModel(
                service = OkHttpPinningService(),
                pinnedUrl = solutionUrl,
                pinnedSet = pinnedSet,
                configurationError = solutionError,
            )
        }
    }

    private val h3Url = BuildConfig.LAB_H3_URL
    private val h3Host = try { URL(h3Url).host } catch (failure: MalformedURLException) { "" }
    private val h3Digests = context.resources.getStringArray(R.array.h3_authority_pins).toList()
    private val h3AuthorityPins = H3Configuration.decodePins(h3Digests)
    private val h3PinnedSet = h3AuthorityPins?.let { PinnedSet(h3Host, h3Digests) }
    private val h3Available = H3Configuration.isAvailable(Build.VERSION.SDK_INT, h3Url, BuildConfig.LAB_H3_HOSTED_URL) &&
        h3PinnedSet != null

    val h3Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val service: H3Service = if (h3Available && Build.VERSION.SDK_INT >= 34 && h3AuthorityPins != null) {
                HttpEnginePinningService(context, h3AuthorityPins)
            } else {
                object : H3Service {
                    override suspend fun fetch(url: String): defdev.labs.droid.pinning.flows.solutionh3.FetchResult =
                        throw LabFailure(LabError.InvalidConfiguration)
                }
            }
            H3ViewModel(service, h3Url, h3PinnedSet, h3Available)
        }
    }
}
