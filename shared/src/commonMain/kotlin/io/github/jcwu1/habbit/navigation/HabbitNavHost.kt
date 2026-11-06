package io.github.jcwu1.habbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.jcwu1.habbit.auth.LoginRoute
import io.github.jcwu1.habbit.auth.RegisterRoute
import io.github.jcwu1.habbit.onboarding.OnboardingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val routeConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Onboarding::class, Onboarding.serializer())
            subclass(Login::class, Login.serializer())
            subclass(Register::class, Register.serializer())
            subclass(Home::class, Home.serializer())
        }
    }
}

@Composable
fun HabbitNavHost(
    hasSeenOnboarding: Boolean,
    isSignedIn: Boolean,
    isSaving: Boolean,
    onCompleteOnboarding: () -> Unit,
    onSignedIn: () -> Unit,
) {
    val root: NavKey = when {
        isSignedIn -> Home
        hasSeenOnboarding -> Login
        else -> Onboarding
    }
    val backStack = rememberNavBackStack(routeConfiguration, root)
    LaunchedEffect(root) {
        if (backStack.firstOrNull() != root) {
            backStack.clear()
            backStack.add(root)
        }
    }
    // Do not render a restored Home entry after the session has expired.
    if (backStack.firstOrNull() != root) return

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Onboarding> {
                OnboardingScreen(
                    onGetStarted = onCompleteOnboarding,
                    onSkip = onCompleteOnboarding,
                    enabled = !isSaving,
                    loading = isSaving,
                )
            }
            entry<Login> {
                LoginRoute(
                    onSignedIn = onSignedIn,
                    onSignUp = {
                        if (backStack.lastOrNull() == Login) backStack.add(Register)
                    },
                )
            }
            entry<Home> { HomeScreen() }
            entry<Register> {
                RegisterRoute(
                    onSignedIn = onSignedIn,
                    onLogIn = {
                        if (backStack.lastOrNull() == Register) backStack.removeLastOrNull()
                    },
                )
            }
        },
    )
}
