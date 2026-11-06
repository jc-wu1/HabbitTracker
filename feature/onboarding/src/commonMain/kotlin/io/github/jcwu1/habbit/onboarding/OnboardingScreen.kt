package io.github.jcwu1.habbit.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import io.github.jcwu1.habbit.ui.components.PrimaryButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.jcwu1.habbit.onboarding.resources.Res
import io.github.jcwu1.habbit.onboarding.resources.img_slide1
import io.github.jcwu1.habbit.onboarding.resources.img_slide2
import io.github.jcwu1.habbit.onboarding.resources.img_slide3
import io.github.jcwu1.habbit.onboarding.resources.onboarding_get_started
import io.github.jcwu1.habbit.onboarding.resources.onboarding_goals_body
import io.github.jcwu1.habbit.onboarding.resources.onboarding_goals_label
import io.github.jcwu1.habbit.onboarding.resources.onboarding_goals_title
import io.github.jcwu1.habbit.onboarding.resources.onboarding_next
import io.github.jcwu1.habbit.onboarding.resources.onboarding_page
import io.github.jcwu1.habbit.onboarding.resources.onboarding_skip
import io.github.jcwu1.habbit.onboarding.resources.onboarding_streak_body
import io.github.jcwu1.habbit.onboarding.resources.onboarding_streak_label
import io.github.jcwu1.habbit.onboarding.resources.onboarding_streak_title
import io.github.jcwu1.habbit.onboarding.resources.onboarding_welcome_body
import io.github.jcwu1.habbit.onboarding.resources.onboarding_welcome_label
import io.github.jcwu1.habbit.onboarding.resources.onboarding_welcome_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private class OnboardingSlide(
    val illustration: DrawableResource,
    val label: StringResource,
    val title: StringResource,
    val body: StringResource,
)

private val slides = listOf(
    OnboardingSlide(
        Res.drawable.img_slide1,
        Res.string.onboarding_welcome_label,
        Res.string.onboarding_welcome_title,
        Res.string.onboarding_welcome_body,
    ),
    OnboardingSlide(
        Res.drawable.img_slide2,
        Res.string.onboarding_goals_label,
        Res.string.onboarding_goals_title,
        Res.string.onboarding_goals_body,
    ),
    OnboardingSlide(
        Res.drawable.img_slide3,
        Res.string.onboarding_streak_label,
        Res.string.onboarding_streak_title,
        Res.string.onboarding_streak_body,
    ),
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit = onGetStarted,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val pagerState = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.lastIndex

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .wrapContentSize(Alignment.TopCenter)
                .widthIn(max = 450.dp)
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(
                onClick = onSkip,
                enabled = enabled && !pagerState.isScrollInProgress,
                modifier = Modifier.align(Alignment.End).padding(end = 24.dp),
            ) {
                Text(stringResource(Res.string.onboarding_skip))
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = enabled,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                OnboardingSlideContent(slides[page])
            }

            val pageDescription = stringResource(
                Res.string.onboarding_page,
                pagerState.currentPage + 1,
                slides.size,
            )
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .semantics { contentDescription = pageDescription },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(slides.size) { page ->
                    Box(
                        Modifier.size(8.dp).background(
                            color = if (page == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = CircleShape,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton(
                text = stringResource(
                    if (isLastPage) Res.string.onboarding_get_started else Res.string.onboarding_next,
                ),
                loading = loading,
                onClick = {
                    if (isLastPage) {
                        onGetStarted()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = enabled && !pagerState.isScrollInProgress,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun OnboardingSlideContent(slide: OnboardingSlide) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val illustrationSize = minOf(maxWidth - 48.dp, (maxHeight * 0.62f).coerceAtLeast(120.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(slide.illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(illustrationSize)
                    .aspectRatio(1f),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(slide.label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(slide.title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(slide.body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}
