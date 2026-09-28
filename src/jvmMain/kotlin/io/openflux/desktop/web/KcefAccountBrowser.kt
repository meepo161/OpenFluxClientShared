package io.openflux.desktop.web

import io.openflux.desktop.model.AccountKind
import io.openflux.desktop.model.YandexDisk
import io.openflux.desktop.service.AccountBrowser
import io.openflux.desktop.ui.BrowserPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/** [AccountBrowser] on the built-in Chromium: one page at a time, cookies wiped on close. */
class KcefAccountBrowser : AccountBrowser {
    private val _page = MutableStateFlow<BrowserPage?>(null)
    override val page: StateFlow<BrowserPage?> = _page.asStateFlow()
    private val current get() = _page.value as? KcefPage

    override val url get() = current?.url.orEmpty()
    override val loading get() = current?.loading ?: false
    override val closed get() = current?.closed ?: true

    override suspend fun open(kind: AccountKind, url: String, cookies: Map<String, String>, onStep: (String) -> Unit) {
        close()
        BuiltInBrowser.clearCookies()
        // The cookies can only be set once the browser runs: open a blank page first.
        val page = BuiltInBrowser.open("about:blank", onStep = onStep)
        BuiltInBrowser.setCookies("https://${kind.cookieDomain.removePrefix(".")}/", cookies, kind.cookieDomain)
        page.load(url)
        _page.value = page
    }

    override suspend fun evaluate(script: String) = current?.evaluate(script) ?: error("Страница закрыта")

    override fun load(url: String) {
        current?.load(url)
    }

    override suspend fun cookies(kind: AccountKind): Map<String, String> {
        val urls = kind.cookieUrls
        val now = Date()
        val all = urls.flatMap { BuiltInBrowser.cookies(it) }
            .filter { !it.hasExpires || it.expires == null || it.expires.after(now) }
        return BuiltInBrowser.selectForUrl(all.map { BuiltInBrowser.CookieValue(it.name, it.value, it.domain.orEmpty()) })
    }

    override fun close() {
        val page = current
        _page.value = null
        if (page != null) {
            page.close()
            BuiltInBrowser.clearCookies()
        }
    }
}
