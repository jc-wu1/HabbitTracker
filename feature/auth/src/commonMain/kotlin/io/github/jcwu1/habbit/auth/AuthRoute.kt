package io.github.jcwu1.habbit.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoute(
    onSignUp: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) = AuthRoute(register = false, viewModel = viewModel, onSwitch = onSignUp, onSignedIn = onSignedIn)

@Composable
fun RegisterRoute(
    onLogIn: () -> Unit,
    onSignedIn: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) = AuthRoute(register = true, viewModel = viewModel, onSwitch = onLogIn, onSignedIn = onSignedIn)

@Composable
private fun AuthRoute(
    register: Boolean,
    viewModel: AuthViewModel,
    onSwitch: () -> Unit,
    onSignedIn: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.message) {
        if (state.message == AuthMessage.SignedIn) {
            viewModel.dismissMessage()
            onSignedIn()
        }
    }
    AuthScreen(
        register = register,
        state = state,
        onUsernameChange = viewModel::updateUsername,
        onNameChange = viewModel::updateName,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = { viewModel.updatePassword(it, register) },
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSubmit = { viewModel.submit(register) },
        onGoogle = viewModel::google,
        onForgotPassword = viewModel::forgotPassword,
        onShowMessage = viewModel::showMessage,
        onDismissMessage = viewModel::dismissMessage,
        onSwitch = onSwitch,
    )
}
