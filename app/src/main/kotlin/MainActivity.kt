package defdev.labs.droid.pinning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labs.droid.pinning.theme.PinningTheme
import defdev.labs.droid.pinning.flows.challenge.PinningViewModel as ChallengeViewModel
import defdev.labs.droid.pinning.flows.solution.PinningViewModel as SolutionViewModel
import defdev.labs.droid.pinning.flows.solutionh3.PinningViewModel as H3ViewModel

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinningTheme {
                val challenge: ChallengeViewModel = viewModel(key = "challenge", factory = container.challengeFactory)
                val solution: SolutionViewModel = viewModel(key = "solution", factory = container.solutionFactory)
                val h3: H3ViewModel = viewModel(key = "solution-h3", factory = container.h3Factory)
                PinningApp(challenge, solution, h3)
            }
        }
    }
}
