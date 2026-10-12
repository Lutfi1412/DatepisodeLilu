package com.lutfilia.rundown.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ---------- Response umum ----------
data class BaseResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: JsonElement? = null
)

// ---------- Tanggal ----------
/** Payload create-tanggal: { "tanggal": "2026-10-11", "label": "makan sushi di mall" } */
data class CreateTanggalRequest(val tanggal: String, val label: String)

data class TanggalListResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: List<TanggalDto>? = null
)

data class TanggalDto(
    val id: Int = 0,
    val tanggal: String? = null,
    val status: String? = null,
    @SerializedName("status_rundown") val statusRundown: String? = null,
    /** Tujuan hari itu. Data lama masih ada yang null. */
    val label: String? = null
)

// ---------- Rundown ----------
/** Payload create-rundown (list), update-rundown, dan try-again. Perhatikan: kuncinya `link_gmap` (tanpa s). */
data class RundownRequest(
    val nama: String,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
    @SerializedName("link_gmap") val linkGmap: String = "",
    val rating: Double? = 0.0
)

data class RundownDetailResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: RundownDetailData? = null
)

data class RundownDetailData(
    val tanggal: String? = null,
    val status: String? = null,
    /** Tujuan hari itu (bisa null untuk data lama). */
    val label: String? = null,
    @SerializedName("status_rundown") val statusRundown: String? = null,
    @SerializedName("total_peringatan") val totalPeringatan: Int? = null,
    val rundown: List<RundownDto>? = null
)

/** Di response kuncinya `link_gmaps` (pakai s). */
data class RundownDto(
    val id: Int = 0,
    val nama: String? = null,
    @SerializedName("jam_mulai") val jamMulai: String? = null,
    @SerializedName("jam_selesai") val jamSelesai: String? = null,
    @SerializedName("link_gmaps") val linkGmaps: String? = null,
    val rating: Double? = null
)

// ---------- Error ----------
data class ErrorDetailResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: ErrorDetailData? = null
)

data class ErrorDetailData(
    val tanggal: String? = null,
    val status: String? = null,
    @SerializedName("status_rundown") val statusRundown: String? = null,
    @SerializedName("total_peringatan") val totalPeringatan: Int? = null,
    val error: List<ErrorDto>? = null
)

data class ErrorDto(
    val id: Int = 0,
    val nama: String? = null,
    @SerializedName("jam_mulai") val jamMulai: String? = null,
    @SerializedName("jam_selesai") val jamSelesai: String? = null,
    @SerializedName("error_detail") val errorDetail: String? = null,
    @SerializedName("link_gmaps") val linkGmaps: String? = null,
    val rating: Double? = null
)

// ---------- Dokumentasi ----------
/** PUT dokumentasi/{tanggalId}: { "link": "https://..." } ; kosong = { "link": "" } */
data class DokumentasiRequest(val link: String)

/** GET / PUT dokumentasi/{tanggalId} */
data class DokumentasiResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: DokumentasiData? = null
)

data class DokumentasiData(
    val id: Int? = null,
    /** null = belum ada dokumentasi */
    val dokumentasi: String? = null
)