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
        }
    }
}

@Composable
fun HabbitNavHost(
    hasSeenOnboarding: Boolean,
    isSaving: Boolean,
    onCompleteOnboarding: () -> Unit,
) {
    val backStack = rememberNavBackStack(
        routeConfiguration,
        if (hasSeenOnboarding) Login else Onboarding,
    )
    LaunchedEffect(hasSeenOnboarding) {
        if (hasSeenOnboarding && backStack.any { it == Onboarding }) {
            backStack.clear()
            backStack.add(Login)
        }
    }
    // A restored onboarding stack may predate a completed preference write.
    if (hasSeenOnboarding && backStack.any { it == Onboarding }) return

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
                    onSignUp = {
                        if (backStack.lastOrNull() == Login) backStack.add(Register)
                    },
                )
            }
            entry<Register> {
                RegisterRoute(
                    onLogIn = {
                        if (backStack.lastOrNull() == Register) backStack.removeLastOrNull()
                    },
                )
            }
        },
    )
}
