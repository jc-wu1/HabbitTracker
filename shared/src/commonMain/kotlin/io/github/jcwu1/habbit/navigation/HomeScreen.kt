package io.github.jcwu1.habbit.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import habbittracker.shared.generated.resources.*
import io.github.jcwu1.habbit.learn.LearnScreen
import io.github.jcwu1.habbit.ui.resources.Res as UiRes
import io.github.jcwu1.habbit.ui.resources.ic_book
import io.github.jcwu1.habbit.ui.resources.ic_layers
import io.github.jcwu1.habbit.ui.resources.ic_person
import io.github.jcwu1.habbit.ui.resources.ic_trophy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private class HomeTab(val key: NavKey, val label: StringResource, val icon: DrawableResource)

private val homeTabs = listOf(
    HomeTab(Learn, Res.string.home_learn, UiRes.drawable.ic_book),
    HomeTab(Practice, Res.string.home_practice, UiRes.drawable.ic_layers),
    HomeTab(League, Res.string.home_league, UiRes.drawable.ic_trophy),
    HomeTab(You, Res.string.home_you, UiRes.drawable.ic_person),
)

private val homeConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Learn::class, Learn.serializer())
            subclass(Practice::class, Practice.serializer())
            subclass(League::class, League.serializer())
            subclass(You::class, You.serializer())
        }
    }
}

@Composable
internal fun HomeScreen() {
    val backStack = rememberNavBackStack(homeConfiguration, Learn)
    val selected = backStack.last()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.widthIn(max = 450.dp).fillMaxWidth(),
                    ) {
                        homeTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = selected == tab.key,
                                onClick = {
                                    if (selected != tab.key) {
                                        // Keep Learn as the root so Back returns to it and its scroll state survives.
                                        while (backStack.size > 1) backStack.removeLastOrNull()
                                        if (tab.key != Learn) backStack.add(tab.key)
                                    }
                                },
                                icon = {
                                    Icon(painterResource(tab.icon), null, Modifier.size(24.dp))
                                },
                                label = { Text(stringResource(tab.label), fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color(0xFFEAE8FF),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavDisplay(
            modifier = Modifier.fillMaxSize().padding(padding),
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider {
                entry<Learn> { LearnScreen() }
                entry<Practice> { PlaceholderPage(Res.string.home_practice) }
                entry<League> { PlaceholderPage(Res.string.home_league) }
                entry<You> { PlaceholderPage(Res.string.home_you) }
            },
        )
    }
}

@Composable
private fun PlaceholderPage(label: StringResource) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(label), style = MaterialTheme.typography.headlineMedium)
    }
}
