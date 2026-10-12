package com.lutfilia.rundown.util

import com.lutfilia.rundown.data.model.RundownItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

object Indo {
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val monthsShort = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
}

fun LocalDate.toIndo(): String = "$dayOfMonth ${Indo.months[monthValue - 1]} $year"
fun LocalDate.dayNameIndo(): String = Indo.days[dayOfWeek.value - 1]
fun LocalDate.toIndoLong(): String = "${dayNameIndo()}, ${toIndo()}"
fun LocalDate.monthShortIndo(): String = Indo.monthsShort[monthValue - 1]

/** Terima "2026-10-11" atau "11 Oktober 2026". */
fun parseTanggal(raw: String?): LocalDate? {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty()) return null
    try {
        return LocalDate.parse(text)
    } catch (e: Exception) {
        // lanjut: coba format Indonesia
    }
    val parts = text.split(" ").filter { it.isNotBlank() }
    if (parts.size < 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = Indo.months.indexOfFirst { it.equals(parts[1], ignoreCase = true) }
    val year = parts[2].toIntOrNull() ?: return null
    if (month < 0) return null
    return try {
        LocalDate.of(year, month + 1, day)
    } catch (e: Exception) {
        null
    }
}

/** Terima "08:30" atau "08:30:00". */
fun parseJam(raw: String?): LocalTime? = try {
    raw?.trim()?.takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it) }
} catch (e: Exception) {
    null
}

fun LocalTime.hm(): String = String.format(Locale.US, "%02d:%02d", hour, minute)
fun LocalTime.hms(): String = String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)

fun Double.ratingText(): String = String.format(Locale.US, "%.1f", this)

enum class Phase { DONE, RUNNING, UPCOMING }

/**
 * Status tiap rundown dihitung LOKAL dari jam di HP (bukan dari API).
 * - tanggal sudah lewat  -> semua selesai
 * - tanggal belum tiba   -> semua akan datang
 * - tanggal hari ini     -> dibandingkan dengan jam sekarang
 */
fun phaseOf(item: RundownItem, date: LocalDate?, now: LocalDateTime): Phase {
    val d = date ?: return Phase.UPCOMING
    val today = now.toLocalDate()
    return when {
        d.isBefore(today) -> Phase.DONE
        d.isAfter(today) -> Phase.UPCOMING
        else -> {
            val t = now.toLocalTime()
            when {
                !t.isBefore(item.end) -> Phase.DONE
                !t.isBefore(item.start) -> Phase.RUNNING
                else -> Phase.UPCOMING
            }
        }
    }
}
