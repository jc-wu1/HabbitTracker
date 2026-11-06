package io.github.jcwu1.habbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.jcwu1.habbit.ui.theme.HabbitTheme

@Preview(name = "Learn", widthDp = 402, heightDp = 874, showBackground = true)
@Preview(name = "Learn - compact", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
internal fun HomeScreenPreview() {
    HabbitTheme { HomeScreen() }
}
