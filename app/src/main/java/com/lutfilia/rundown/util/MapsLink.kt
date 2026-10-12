package com.lutfilia.rundown.util

/**
 * Merapikan link Google Maps sebelum dipakai.
 * Tombol "Bagikan" di aplikasi Google Maps (Android) biasanya menghasilkan teks
 * "Nama tempat + alamat + link", bukan link saja. Fungsi ini mengambil URL-nya saja.
 * Link web (google.com/maps/...) maupun link Android (maps.app.goo.gl/...) sama-sama diterima.
 */
object MapsLink {
    private val urlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    private val bareRegex = Regex(
        """(?:maps\.app\.goo\.gl|goo\.gl/maps|(?:www\.)?google\.[a-z.]+/maps)[^\s<>"']*""",
        RegexOption.IGNORE_CASE
    )

    /** Ambil URL dari teks hasil copy/share. Kalau tidak ada URL, teks dikembalikan apa adanya. */
    fun extract(raw: String): String {
        val text = raw.trim()
        if (text.isEmpty()) return ""
        val found = urlRegex.find(text)?.value
            ?: bareRegex.find(text)?.value?.let { "https://$it" }
        return (found ?: text).trimEnd('.', ',', ';', '!')
    }
}