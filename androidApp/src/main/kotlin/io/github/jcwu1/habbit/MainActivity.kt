package io.github.jcwu1.habbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.jcwu1.habbit.startup.AppViewModel
import io.github.jcwu1.habbit.onboarding.OnboardingScreen
import io.github.jcwu1.habbit.ui.theme.HabbitTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appViewModel.load()
        splash.setKeepOnScreenCondition {
            val state = appViewModel.state.value
            state.hasSeenOnboarding == null && !state.loadFailed
        }

        setContent {
            App(appViewModel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    HabbitTheme { OnboardingScreen(onGetStarted = {}) }
}