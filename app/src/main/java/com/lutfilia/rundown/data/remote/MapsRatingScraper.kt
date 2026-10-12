package com.lutfilia.rundown.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

/**
 * Scraping rating Google Maps di sisi aplikasi (FE), bukan di server.
 *
 * Cara kerja: halaman tempat dibuka di WebView tersembunyi (tampilan web desktop), ditunggu sampai
 * rating muncul di halaman, lalu angkanya dibaca lewat JavaScript. Karena yang dibaca adalah halaman
 * yang sudah dirender, link web (google.com/maps/...) dan link Android (maps.app.goo.gl/...)
 * sama-sama bisa, dan hasilnya sama dengan yang tampil di browser.
 *
 * Mengembalikan null kalau tempat tidak punya rating / halaman gagal dibaca (pemanggil mengisi 0.0).
 * Log untuk debugging: Logcat, filter tag "MapsRating".
 */
class MapsRatingScraper(private val context: Context) {

    private val mutex = Mutex()                       // satu WebView dalam satu waktu (hemat memori)
    private val cache = HashMap<String, Double>()     // link -> rating (kegagalan tidak di-cache)

    suspend fun fetchRating(link: String): Double? = mutex.withLock {
        cache[link]?.let { return@withLock it }
        val rating = try {
            withContext(Dispatchers.Main) { scrape(link) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "scrape gagal: ${e.message}")
            null
        }
        if (rating != null) cache[link] = rating
        Log.d(TAG, "link=$link rating=$rating")
        rating
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun scrape(link: String): Double? {
        val web = WebView(context)
        try {
            with(web.settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = false
                userAgentString = DESKTOP_UA
            }
            // WebView yang tidak ditempel ke layar tetap perlu ukuran supaya halaman dirender.
            web.measure(
                View.MeasureSpec.makeMeasureSpec(1280, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1800, View.MeasureSpec.EXACTLY)
            )
            web.layout(0, 0, 1280, 1800)
            web.onResume()
            web.resumeTimers()
            web.webViewClient = WebViewClient()
            web.loadUrl(link, mapOf("Accept-Language" to "en-US,en;q=0.9"))

            return withTimeoutOrNull(TOTAL_TIMEOUT_MS) {
                var result: Double? = null
                var finished = false
                var titleSeenAt = -1L
                val startedAt = System.currentTimeMillis()
                while (!finished) {
                    delay(POLL_MS)
                    val state = evalJs(web, READ_JS)?.trim('"').orEmpty()
                    val elapsed = System.currentTimeMillis() - startedAt
                    Log.d(TAG, "t=${elapsed}ms state=$state url=${web.url}")
                    when {
                        state.startsWith("R:") -> {
                            val value = state.substring(2).replace(',', '.').toDoubleOrNull()
                            if (value != null && value in 0.0..5.0) {
                                result = value
                                finished = true
                            }
                        }
                        state == "T" -> {
                            // Halaman tempat sudah tampil tapi rating belum ada: beri waktu sebentar.
                            if (titleSeenAt < 0) titleSeenAt = elapsed
                            if (elapsed - titleSeenAt > NO_RATING_GRACE_MS) finished = true
                        }
                    }
                }
                result
            }
        } finally {
            web.stopLoading()
            web.destroy()
        }
    }

    private suspend fun evalJs(web: WebView, js: String): String? =
        suspendCancellableCoroutine { cont ->
            web.evaluateJavascript(js) { result -> cont.resume(result) }
        }

    private companion object {
        const val TAG = "MapsRating"
        const val TOTAL_TIMEOUT_MS = 20_000L
        const val NO_RATING_GRACE_MS = 5_000L
        const val POLL_MS = 800L

        const val DESKTOP_UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/124.0.0.0 Safari/537.36"

        /**
         * Hasil: "R:1.7" = rating ketemu, "T" = judul tempat sudah ada tapi rating belum, "N" = belum siap,
         * "C" = halaman consent Google (otomatis diklik), "E" = error JS.
         * Rating dicari dari aria-label seperti "1.7 stars" / "1,7 bintang" (tahan beda bahasa).
         */
        val READ_JS = """
            (function(){
              try {
                if (location.hostname.indexOf('consent.') === 0) {
                  var b = document.querySelector('button[aria-label*="Reject"],button[aria-label*="Accept"],form[action*="consent"] button');
                  if (b) { b.click(); }
                  return 'C';
                }
                var els = document.querySelectorAll('[aria-label]');
                for (var i = 0; i < els.length; i++) {
                  var l = els[i].getAttribute('aria-label') || '';
                  var m = l.match(/^\s*([0-5][.,][0-9])\s*(stars?|bintang)/i);
                  if (m) { return 'R:' + m[1]; }
                }
                var f = document.querySelector('div.F7nice span[aria-hidden="true"]');
                if (f) {
                  var t = (f.textContent || '').trim();
                  if (/^[0-5][.,][0-9]$/.test(t)) { return 'R:' + t; }
                }
                var h = document.querySelector('h1');
                return (h && h.textContent.trim().length > 0) ? 'T' : 'N';
              } catch (e) { return 'E'; }
            })()
        """.trimIndent()
    }
}