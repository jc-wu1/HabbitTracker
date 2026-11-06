package io.github.jcwu1.habbit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jcwu1.habbit.startup.AppViewModel
import io.github.jcwu1.habbit.ui.AppContent
import io.github.jcwu1.habbit.ui.theme.HabbitTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.load() }
    HabbitTheme {
        AppContent(
            state = state,
            onRetryLoad = viewModel::load,
            onCompleteOnboarding = viewModel::completeOnboarding,
            onDismissSaveError = viewModel::dismissSaveError,
        )
    }
}
