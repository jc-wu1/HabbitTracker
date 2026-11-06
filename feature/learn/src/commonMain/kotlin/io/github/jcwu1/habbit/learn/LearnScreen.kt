package io.github.jcwu1.habbit.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jcwu1.habbit.learn.resources.*
import io.github.jcwu1.habbit.ui.resources.Res as UiRes
import io.github.jcwu1.habbit.ui.resources.ic_arrow_forward
import io.github.jcwu1.habbit.ui.resources.ic_check
import io.github.jcwu1.habbit.ui.resources.ic_fire
import io.github.jcwu1.habbit.ui.resources.ic_lock
import io.github.jcwu1.habbit.ui.resources.ic_notifications
import io.github.jcwu1.habbit.ui.resources.ic_play
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LearnScreen(modifier: Modifier = Modifier) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lessonMessage = stringResource(Res.string.learn_lessons_unavailable)
    val unitsMessage = stringResource(Res.string.learn_units_unavailable)
    val notificationsMessage = stringResource(Res.string.learn_notifications_unavailable)
    val showLesson = { scope.launch { snackbar.showSnackbar(lessonMessage) }; Unit }

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .wrapContentSize(Alignment.TopCenter).widthIn(max = 450.dp)
                .fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        stringResource(Res.string.learn_greeting),
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(Res.string.learn_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { scope.launch { snackbar.showSnackbar(notificationsMessage) } },
                ) {
                    Icon(
                        painterResource(UiRes.drawable.ic_notifications),
                        stringResource(Res.string.learn_notifications),
                        Modifier.size(24.dp),
                    )
                }
            }
            ContinueLearningCard(onContinue = showLesson)
            StreakCard()
            LearningPathCard(
                onContinue = showLesson,
                onViewAll = { scope.launch { snackbar.showSnackbar(unitsMessage) } },
            )
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ContinueLearningCard(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF393080), Color(0xFF6964E8))))
            .padding(18.dp),
    ) {
        Text(
            stringResource(Res.string.learn_continue_label),
            color = Color(0xFFEAE8FF),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.learn_lesson_title),
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(Res.string.learn_unit),
                    color = Color(0xFFF0EEFF),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(Res.string.learn_lesson_duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
            Image(
                painterResource(Res.drawable.img_lesson),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.3f).aspectRatio(1f).clip(RoundedCornerShape(24.dp)),
            )
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(Res.string.learn_continue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Icon(
                    painterResource(UiRes.drawable.ic_arrow_forward),
                    null,
                    Modifier.align(Alignment.CenterEnd).size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun StreakCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE0D8FF), Color(0xFFFFCBB4)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(UiRes.drawable.ic_fire),
                        null,
                        Modifier.size(24.dp),
                        tint = Color(0xFF7252C4),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        stringResource(Res.string.learn_streak),
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(Res.string.learn_streak_body),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.learn_goal), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(Res.string.learn_goal_time), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { 10f / 15f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFFDDDBF7),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun LearningPathCard(onContinue: () -> Unit, onViewAll: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.learn_path),
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onViewAll, contentPadding = PaddingValues(start = 8.dp)) {
                    Text(stringResource(Res.string.learn_view_all), fontSize = 11.sp)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Image(
                    painterResource(Res.drawable.img_lesson),
                    null,
                    Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)),
                )
                Text(
                    stringResource(Res.string.learn_path_unit),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(4.dp))
            PathLesson(
                title = stringResource(Res.string.learn_greetings),
                status = stringResource(Res.string.learn_complete),
                icon = UiRes.drawable.ic_check,
                iconColor = Color(0xFF426C50),
                iconBackground = Color(0xFFE7EFE8),
                connected = true,
            )
            PathLesson(
                title = stringResource(Res.string.learn_lesson_title),
                status = stringResource(Res.string.learn_current),
                icon = UiRes.drawable.ic_play,
                iconColor = Color.White,
                iconBackground = MaterialTheme.colorScheme.primary,
                connected = true,
                current = true,
                onClick = onContinue,
            )
            PathLesson(
                title = stringResource(Res.string.learn_ordering_food),
                status = stringResource(Res.string.learn_up_next),
                icon = UiRes.drawable.ic_lock,
                iconColor = Color(0xFF8990A8),
                iconBackground = Color(0xFFEBEDF3),
                connected = false,
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Text(
                stringResource(Res.string.learn_path_footer),
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PathLesson(
    title: String,
    status: String,
    icon: DrawableResource,
    iconColor: Color,
    iconBackground: Color,
    connected: Boolean,
    current: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = if (current) 48.dp else 36.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(26.dp).height(if (current) 48.dp else 36.dp)) {
            if (connected) {
                Box(
                    Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                        .width(1.dp).fillMaxHeight().background(Color(0xFFE4E6EF)),
                )
            }
            Box(
                Modifier.align(Alignment.Center).size(18.dp).background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), null, Modifier.size(14.dp), tint = iconColor)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            Modifier.weight(1f).padding(end = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
            color = if (icon == UiRes.drawable.ic_lock) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            status,
            fontSize = 10.sp,
            color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
