package defdev.labs.droid.pinning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import defdev.labs.droid.pinning.theme.PinningTheme
import defdev.labs.droid.pinning.flows.challenge.PinningViewModel as ChallengeViewModel
import defdev.labs.droid.pinning.flows.solution.PinningViewModel as SolutionViewModel

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinningTheme {
                val challenge: ChallengeViewModel = viewModel(key = "challenge", factory = container.challengeFactory)
                val solution: SolutionViewModel = viewModel(key = "solution", factory = container.solutionFactory)
                PinningApp(challenge, solution)
            }
        }
    }
}
