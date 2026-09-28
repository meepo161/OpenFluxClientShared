package io.openflux.desktop

import io.openflux.desktop.model.AccountKind
import io.openflux.desktop.web.KcefAccountBrowser
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test

/**
 * Opens every service's sign-in page in the real built-in browser (the
 * Chromium installed in %LOCALAPPDATA%/OpenFlux/browser) the way the
 * Accounts tab does; OPENFLUX_LIVE_BROWSER=1 runs it.
 */
class BrowserLiveTest {
    @Test
    fun signInPagesLoad() = runBlocking {
        if (System.getenv("OPENFLUX_LIVE_BROWSER") != "1") return@runBlocking
        val browser = KcefAccountBrowser()
        for (kind in AccountKind.entries.filter { it.signInUrl.startsWith("http") }) {
            browser.open(kind, kind.signInUrl, emptyMap()) { println("  step: $it") }
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 25_000 && (browser.loading || browser.url.isEmpty() || browser.url == "about:blank")) delay(250)
            delay(1500)
            val probe = withTimeoutOrNull(20_000) {
                runCatching {
                    browser.evaluate("JSON.stringify({ua: navigator.userAgent, title: document.title, text: (document.body && document.body.innerText || '').length})")
                }.getOrElse { "error: ${it.message}" }
            }
            println("$kind: ${System.currentTimeMillis() - start} ms url=${browser.url.take(90)} loading=${browser.loading}\n  $probe")
            browser.close()
        }
    }
}
