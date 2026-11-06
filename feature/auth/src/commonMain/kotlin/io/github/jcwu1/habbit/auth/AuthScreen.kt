package io.github.jcwu1.habbit.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jcwu1.habbit.auth.resources.*
import io.github.jcwu1.habbit.ui.resources.Res as UiRes
import io.github.jcwu1.habbit.ui.resources.common_ok
import io.github.jcwu1.habbit.ui.components.PrimaryButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AuthScreen(
    register: Boolean,
    state: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogle: () -> Unit,
    onForgotPassword: () -> Unit,
    onShowMessage: (AuthMessage) -> Unit,
    onDismissMessage: () -> Unit,
    onSwitch: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val submit = {
        keyboard?.hide()
        focus.clearFocus()
        onSubmit()
    }
    val linkStyles = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary))

    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier.safeDrawingPadding().imePadding()
                .wrapContentSize(Alignment.TopCenter).widthIn(max = 450.dp).fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight).padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Image(
                        painterResource(Res.drawable.img_header),
                        contentDescription = "Habbit",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().aspectRatio(354f / 96f),
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        stringResource(if (register) Res.string.auth_register_title else Res.string.auth_login_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(if (register) Res.string.auth_register_subtitle else Res.string.auth_login_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(20.dp))
                    if (register) {
                        AuthField(
                            value = state.name,
                            onValueChange = onNameChange,
                            label = stringResource(Res.string.auth_name),
                            error = state.errors.name,
                            enabled = !state.isLoading,
                            keyboardType = KeyboardType.Text,
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                    AuthField(
                        value = if (register) state.email else state.username,
                        onValueChange = if (register) onEmailChange else onUsernameChange,
                        label = stringResource(if (register) Res.string.auth_email else Res.string.auth_username),
                        error = if (register) state.errors.email else state.errors.username,
                        enabled = !state.isLoading,
                        keyboardType = if (register) KeyboardType.Email else KeyboardType.Text,
                    )
                    Spacer(Modifier.height(20.dp))
                    AuthField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = stringResource(Res.string.auth_password),
                        error = state.errors.password,
                        enabled = !state.isLoading,
                        keyboardType = KeyboardType.Password,
                        helper = if (register) stringResource(Res.string.auth_password_helper) else null,
                        passwordVisible = state.passwordVisible,
                        onTogglePassword = onTogglePasswordVisibility,
                        onDone = submit,
                    )
                    if (!register) {
                        TextButton(
                            onClick = onForgotPassword,
                            enabled = !state.isLoading,
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            Text(stringResource(Res.string.auth_forgot_password), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(if (register) 16.dp else 8.dp))
                    PrimaryButton(
                        text = stringResource(if (register) Res.string.auth_create_account else Res.string.auth_log_in),
                        onClick = submit,
                        loading = state.isLoading,
                    )
                    if (register) {
                        Spacer(Modifier.height(10.dp))
                        val intro = stringResource(Res.string.auth_terms_intro)
                        val terms = stringResource(Res.string.auth_terms)
                        val and = stringResource(Res.string.auth_and)
                        val privacy = stringResource(Res.string.auth_privacy)
                        Text(
                            buildAnnotatedString {
                                append(intro)
                                append(" ")
                                withLink(LinkAnnotation.Clickable("terms", linkStyles) {
                                    onShowMessage(AuthMessage.TermsUnavailable)
                                }) { append(terms) }
                                append("\n$and ")
                                withLink(LinkAnnotation.Clickable("privacy", linkStyles) {
                                    onShowMessage(AuthMessage.PrivacyUnavailable)
                                }) { append(privacy) }
                                append(".")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(stringResource(Res.string.auth_or), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    OutlinedButton(
                        onClick = onGoogle,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Image(painterResource(Res.drawable.ic_google), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(stringResource(Res.string.auth_google), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                val footer = stringResource(if (register) Res.string.auth_has_account else Res.string.auth_no_account)
                val switch = stringResource(if (register) Res.string.auth_log_in else Res.string.auth_sign_up)
                Text(
                    buildAnnotatedString {
                        append("$footer ")
                        withLink(LinkAnnotation.Clickable("switch", linkStyles) { onSwitch() }) { append(switch) }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissMessage,
            title = {
                Text(stringResource(when (message) {
                    AuthMessage.SignedIn -> Res.string.auth_success_title
                    AuthMessage.InvalidCredentials, AuthMessage.StorageFailed, AuthMessage.Failed -> Res.string.auth_error_title
                    else -> Res.string.auth_unavailable_title
                }))
            },
            text = {
                Text(stringResource(when (message) {
                    AuthMessage.SignedIn -> Res.string.auth_success_body
                    AuthMessage.InvalidCredentials -> Res.string.auth_invalid_credentials
                    AuthMessage.StorageFailed -> Res.string.auth_storage_failed
                    AuthMessage.NotConfigured -> Res.string.auth_unavailable_body
                    AuthMessage.Failed -> Res.string.auth_failed
                    AuthMessage.TermsUnavailable -> Res.string.auth_terms_unavailable
                    AuthMessage.PrivacyUnavailable -> Res.string.auth_privacy_unavailable
                }))
            },
            confirmButton = {
                TextButton(onClick = onDismissMessage) { Text(stringResource(UiRes.string.common_ok)) }
            },
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: FieldError?,
    enabled: Boolean,
    keyboardType: KeyboardType,
    helper: String? = null,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {
    val focus = LocalFocusManager.current
    val supporting = when (error) {
        FieldError.Required -> stringResource(Res.string.auth_required)
        FieldError.InvalidEmail -> stringResource(Res.string.auth_invalid_email)
        FieldError.WeakPassword -> stringResource(Res.string.auth_password_helper)
        null -> helper
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        supportingText = supporting?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        visualTransformation = if (keyboardType == KeyboardType.Password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (onDone == null) ImeAction.Next else ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focus.moveFocus(FocusDirection.Next) },
            onDone = { onDone?.invoke() },
        ),
        trailingIcon = onTogglePassword?.let { toggle ->
            {
                IconButton(onClick = toggle, enabled = enabled) {
                    Icon(
                        painterResource(if (passwordVisible) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility),
                        contentDescription = stringResource(if (passwordVisible) Res.string.auth_hide_password else Res.string.auth_show_password),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}
