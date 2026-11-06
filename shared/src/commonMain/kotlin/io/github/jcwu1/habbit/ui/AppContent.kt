package io.github.jcwu1.habbit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import habbittracker.shared.generated.resources.*
import io.github.jcwu1.habbit.navigation.HabbitNavHost
import io.github.jcwu1.habbit.ui.resources.Res as UiRes
import io.github.jcwu1.habbit.ui.resources.common_ok
import io.github.jcwu1.habbit.startup.StartupState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppContent(
    state: StartupState,
    onRetryLoad: () -> Unit,
    onSignedIn: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    onDismissSaveError: () -> Unit,
) {
    val seen = state.hasSeenOnboarding
    if (seen == null) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.loadFailed) {
                    TextButton(onClick = onRetryLoad) {
                        Text(stringResource(Res.string.startup_load_failed) + " " + stringResource(Res.string.startup_retry))
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    } else {
        HabbitNavHost(
            hasSeenOnboarding = seen,
            isSignedIn = state.isSignedIn,
            isSaving = state.isSaving,
            onCompleteOnboarding = onCompleteOnboarding,
            onSignedIn = onSignedIn,
        )
    }
    if (state.saveFailed) {
        AlertDialog(
            onDismissRequest = onDismissSaveError,
            text = { Text(stringResource(Res.string.startup_save_failed)) },
            confirmButton = {
                TextButton(onClick = onCompleteOnboarding) {
                    Text(stringResource(Res.string.startup_retry))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSaveError) { Text(stringResource(UiRes.string.common_ok)) }
            },
        )
    }
}
