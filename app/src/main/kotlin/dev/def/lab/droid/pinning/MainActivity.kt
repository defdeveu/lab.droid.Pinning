package dev.def.lab.droid.pinning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.def.lab.droid.pinning.theme.PinningTheme
import dev.def.lab.droid.pinning.ui.PinningScreen

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinningTheme {
                val viewModel: PinningViewModel = viewModel(factory = container.viewModelFactory)
                PinningScreen(viewModel = viewModel)
            }
        }
    }
}
