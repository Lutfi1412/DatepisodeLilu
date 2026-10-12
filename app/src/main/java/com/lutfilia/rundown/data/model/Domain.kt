package com.lutfilia.rundown.data.model

import java.time.LocalDate
import java.time.LocalTime

/** Model yang dipakai UI. */
data class DateItem(
    val id: Int,
    /** Teks tanggal dari server, mis. "14 Oktober 2026" (dulu bernama `label`). */
    val dateText: String,
    /** Tujuan hari itu, mis. "makan sushi di mall". Bisa null (data lama). */
    val label: String?,
    val date: LocalDate?,
    val status: String,
    val statusRundown: String
) {
    val isFinished: Boolean
        get() = status.contains("selesai") || (date != null && date.isBefore(LocalDate.now()))
}

data class RundownItem(
    val id: Int,
    val nama: String,
    val start: LocalTime,
    val end: LocalTime,
    val link: String,
    val rating: Double?
)

data class DateDetail(
    val date: LocalDate?,
    /** Teks tanggal dari server (dulu bernama `label`). */
    val dateText: String,
    /** Tujuan hari itu. Bisa null. */
    val label: String?,
    val status: String,
    val statusRundown: String,
    val totalWarning: Int,
    val items: List<RundownItem>
)

data class ErrorItem(
    val id: Int,
    val nama: String,
    val start: LocalTime?,
    val end: LocalTime?,
    val detail: String,
    val link: String,
    val rating: Double?
)

data class ErrorDetail(
    val date: LocalDate?,
    val totalWarning: Int,
    val items: List<ErrorItem>
)

/** Data yang dikirim ke server (create / update / try-again). */
data class RundownDraft(
    val nama: String,
    val start: LocalTime,
    val end: LocalTime,
    val link: String = "",
    val rating: Double? = null
)

fun RundownItem.toDraft() = RundownDraft(nama, start, end, link, rating)

fun ErrorItem.toDraftOrNull(): RundownDraft? {
    val s = start ?: return null
    val e = end ?: return null
    return RundownDraft(nama, s, e, link, rating)
}