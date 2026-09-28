package io.openflux.desktop.ui.accounts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.openflux.desktop.model.AuthStatus
import io.openflux.desktop.service.LocalAppContainer
import io.openflux.desktop.ui.LocalBrowserViews
import io.openflux.desktop.ui.components.AppDialog
import io.openflux.desktop.ui.components.fillUpTo
import io.openflux.desktop.ui.components.windowSize
import io.openflux.desktop.ui.theme.AppTheme

/**
 * The service's own page in the built-in browser while the user signs in
 * or a document is being created. Closing it cancels.
 */
@Composable
fun SignInDialog() {
    val accounts = LocalAppContainer.current.accounts
    val kind by accounts.signingIn.collectAsState()
    val page by accounts.page.collectAsState()
    val status by accounts.status.collectAsState()
    val current = kind ?: return
    val window = windowSize()
    val pageHeight = (window.height - 300.dp).coerceIn(220.dp, 680.dp)
    val step = (status[current] as? AuthStatus.Busy)?.step.orEmpty()
    AppDialog(
        modifier = Modifier.fillUpTo(if (window.width >= 1400.dp) 960.dp else 760.dp),
        title = "Вход в ${current.label}",
        onDismiss = accounts::cancel,
        secondary = "Отмена",
        enterSubmits = false,
    ) {
        Text(
            "Войдите в свой аккаунт. OpenFlux сохранит только сессию — на этом устройстве и на ваших нодах; " +
                "пароль не сохраняется. Окно закроется само.",
            style = AppTheme.typography.body,
            color = AppTheme.colors.text,
        )
        if (step.isNotEmpty()) {
            Spacer(Modifier.height(AppTheme.spacing.s))
            Text(step, style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
        }
        Spacer(Modifier.height(AppTheme.spacing.m))
        Box(
            Modifier.fillMaxWidth().height(pageHeight).clip(AppTheme.shapes.card)
                .border(1.dp, AppTheme.colors.border, AppTheme.shapes.card),
            contentAlignment = Alignment.Center,
        ) {
            val shown = page
            if (shown != null) LocalBrowserViews.current.Page(shown, Modifier.fillMaxSize())
            else Text(step.ifEmpty { "Открываю страницу входа…" }, style = AppTheme.typography.body, color = AppTheme.colors.textSecondary)
        }
    }
}
