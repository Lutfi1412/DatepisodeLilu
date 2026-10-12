package com.lutfilia.rundown.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lutfilia.rundown.data.model.BaseResponse
import com.lutfilia.rundown.data.model.CreateTanggalRequest
import com.lutfilia.rundown.data.model.DokumentasiRequest
import com.lutfilia.rundown.data.model.DateDetail
import com.lutfilia.rundown.data.model.DateItem
import com.lutfilia.rundown.data.model.ErrorDetail
import com.lutfilia.rundown.data.model.ErrorItem
import com.lutfilia.rundown.data.model.RundownDraft
import com.lutfilia.rundown.data.model.RundownItem
import com.lutfilia.rundown.data.model.RundownRequest
import com.lutfilia.rundown.data.remote.ApiService
import com.lutfilia.rundown.data.remote.MapsRatingScraper
import com.lutfilia.rundown.util.MapsLink
import com.lutfilia.rundown.util.hm
import com.lutfilia.rundown.util.hms
import com.lutfilia.rundown.util.parseJam
import com.lutfilia.rundown.util.parseTanggal
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import kotlin.coroutines.cancellation.CancellationException

class RundownRepository(
    private val api: ApiService,
    private val ratingScraper: MapsRatingScraper
) {


    // ------------------------------------------------------------------ Tanggal
    suspend fun getTanggal(): ApiResult<List<DateItem>> = execute({ api.getTanggal() }) { body ->
        body?.data.orEmpty().map { dto ->
            DateItem(
                id = dto.id,
                dateText = dto.tanggal.orEmpty(),
                label = dto.label?.trim()?.takeIf { it.isNotEmpty() },
                date = parseTanggal(dto.tanggal),
                status = dto.status.orEmpty().lowercase(),
                statusRundown = dto.statusRundown.orEmpty().lowercase()
            )
        }
    }

    /**
     * Membuat tanggal lalu mengembalikan id-nya (dibutuhkan untuk create-rundown/{id}).
     * Response create-tanggal belum didokumentasikan, jadi:
     *  1) coba baca data.id dari response,
     *  2) kalau tidak ada, ambil get-tanggal lalu cari tanggal yang cocok.
     */
    suspend fun createTanggal(date: LocalDate, label: String): ApiResult<Int> {
        val cleanLabel = label.trim()
        when (val res = simple { api.createTanggal(CreateTanggalRequest(date.toString(), cleanLabel)) }) {
            is ApiResult.Failure -> return res
            is ApiResult.Success -> {
                extractId(res.data.data)?.let { return ApiResult.Success(it) }
            }
        }
        return when (val list = getTanggal()) {
            is ApiResult.Failure -> list
            is ApiResult.Success -> {
                // bisa ada beberapa entri di tanggal yang sama: utamakan yang labelnya cocok
                val sameDate = list.data.filter { it.date == date }
                val match = sameDate.firstOrNull { it.label.equals(cleanLabel, ignoreCase = true) }
                    ?: sameDate.maxByOrNull { it.id }
                    ?: list.data.maxByOrNull { it.id }
                if (match != null) ApiResult.Success(match.id)
                else ApiResult.Failure("Tanggal sudah dibuat, tapi id-nya tidak ditemukan. Tarik layar untuk refresh.")
            }
        }
    }

    suspend fun deleteTanggal(id: Int): ApiResult<Unit> = simple { api.deleteTanggal(id) }.asUnit()

    // ------------------------------------------------------------------ Rundown
    /**
     * Create: jam format HH:mm. Rating di-scraping oleh aplikasi dari link_gmap;
     * tidak ada rating / link kosong -> 0.0.
     */
    suspend fun createRundown(tanggalId: Int, drafts: List<RundownDraft>): ApiResult<Unit> {
        val ready = drafts.map { fillRating(it, rateIfNoLink = 0.0) }
        return simple {
            api.createRundown(tanggalId, ready.map { it.toRequest(withSeconds = false, ratingFallback = 0.0) })
        }.asUnit()
    }

    suspend fun getRundown(tanggalId: Int): ApiResult<DateDetail> = execute({ api.getRundown(tanggalId) }) { body ->
        val d = body?.data
        DateDetail(
            date = parseTanggal(d?.tanggal),
            dateText = d?.tanggal.orEmpty(),
            label = d?.label?.trim()?.takeIf { it.isNotEmpty() },
            status = d?.status.orEmpty().lowercase(),
            statusRundown = d?.statusRundown.orEmpty().lowercase(),
            totalWarning = d?.totalPeringatan ?: 0,
            items = d?.rundown.orEmpty().map { r ->
                RundownItem(
                    id = r.id,
                    nama = r.nama.orEmpty(),
                    start = parseJam(r.jamMulai) ?: LocalTime.MIDNIGHT,
                    end = parseJam(r.jamSelesai) ?: LocalTime.MIDNIGHT,
                    link = r.linkGmaps.orEmpty(),
                    rating = r.rating
                )
            }.sortedBy { it.start }
        )
    }

    /** Update: jam format HH:mm:ss. Link kosong -> rating null (sesuai contoh API). */
    suspend fun updateRundown(rundownId: Int, draft: RundownDraft): ApiResult<Unit> {
        val ready = fillRating(draft, rateIfNoLink = null)
        return simple { api.updateRundown(rundownId, ready.toRequest(withSeconds = true, ratingFallback = null)) }.asUnit()
    }

    suspend fun deleteRundown(ids: List<Int>): ApiResult<Unit> = simple { api.deleteRundown(ids) }.asUnit()

    // ------------------------------------------------------------------ Dokumentasi
    /** Link dokumentasi tanggal ini. Belum ada (null dari server) -> string kosong. */
    suspend fun getDokumentasi(tanggalId: Int): ApiResult<String> =
        execute({ api.getDokumentasi(tanggalId) }) { body ->
            body?.data?.dokumentasi?.trim().orEmpty()
        }

    /** Simpan link dokumentasi. link kosong = hapus. Mengembalikan link yang tersimpan di server. */
    suspend fun updateDokumentasi(tanggalId: Int, link: String): ApiResult<String> {
        val sent = link.trim()
        return when (val res = execute({ api.putDokumentasi(tanggalId, DokumentasiRequest(sent)) }) { it }) {
            is ApiResult.Failure -> res
            is ApiResult.Success -> {
                val body = res.data
                if (body?.status == false) ApiResult.Failure(body.message ?: "Gagal menyimpan dokumentasi")
                else ApiResult.Success(body?.data?.let { it.dokumentasi?.trim().orEmpty() } ?: sent)
            }
        }
    }

    // ------------------------------------------------------------------ Error
    suspend fun getError(tanggalId: Int): ApiResult<ErrorDetail> = execute({ api.getError(tanggalId) }) { body ->
        val d = body?.data
        ErrorDetail(
            date = parseTanggal(d?.tanggal),
            totalWarning = d?.totalPeringatan ?: 0,
            items = d?.error.orEmpty().map { e ->
                ErrorItem(
                    id = e.id,
                    nama = e.nama.orEmpty(),
                    start = parseJam(e.jamMulai),
                    end = parseJam(e.jamSelesai),
                    detail = e.errorDetail.orEmpty(),
                    link = e.linkGmaps.orEmpty(),
                    rating = e.rating
                )
            }
        )
    }

    suspend fun deleteErrors(ids: List<Int>): ApiResult<Unit> = simple { api.deleteError(ids) }.asUnit()

    suspend fun tryAgain(errorId: Int, tanggalId: Int, draft: RundownDraft): ApiResult<Unit> {
        val ready = fillRating(draft, rateIfNoLink = 0.0)
        return simple {
            api.tryAgain(errorId, tanggalId, ready.toRequest(withSeconds = false, ratingFallback = 0.0))
        }.asUnit()
    }

    // ------------------------------------------------------------------ helper
    /**
     * Rapikan link, lalu isi rating:
     *  - link kosong                        -> rating lama, kalau tidak ada pakai [rateIfNoLink]
     *  - sudah ada rating (> 0) & link sama -> dipakai lagi, tidak scraping ulang
     *  - selain itu                         -> scraping dari link; tidak ketemu -> 0.0
     */
    private suspend fun fillRating(draft: RundownDraft, rateIfNoLink: Double?): RundownDraft {
        val link = MapsLink.extract(draft.link)
        if (link.isBlank()) return draft.copy(link = "", rating = draft.rating ?: rateIfNoLink)
        val existing = draft.rating
        if (existing != null && existing > 0.0) return draft.copy(link = link)
        val scraped = ratingScraper.fetchRating(link)
        return draft.copy(link = link, rating = scraped ?: 0.0)
    }

    private fun RundownDraft.toRequest(withSeconds: Boolean, ratingFallback: Double?) = RundownRequest(
        nama = nama.trim(),
        jamMulai = if (withSeconds) start.hms() else start.hm(),
        jamSelesai = if (withSeconds) end.hms() else end.hm(),
        linkGmap = link.trim(),
        rating = rating ?: ratingFallback
    )

    private fun extractId(el: JsonElement?): Int? = try {
        if (el != null && el.isJsonObject) {
            val id = el.asJsonObject.get("id")
            if (id != null && !id.isJsonNull) id.asInt else null
        } else null
    } catch (e: Exception) {
        null
    }

    private fun ApiResult<BaseResponse>.asUnit(): ApiResult<Unit> = when (this) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> this
    }

    private suspend fun simple(block: suspend () -> Response<BaseResponse>): ApiResult<BaseResponse> {
        val res = execute(block) { it ?: BaseResponse() }
        return if (res is ApiResult.Success && res.data.status == false) {
            ApiResult.Failure(res.data.message ?: "Permintaan ditolak server")
        } else res
    }

    private suspend fun <R, T> execute(
        block: suspend () -> Response<R>,
        map: (R?) -> T
    ): ApiResult<T> = try {
        val res = block()
        if (res.isSuccessful) ApiResult.Success(map(res.body()))
        else ApiResult.Failure(errorMessage(res))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        ApiResult.Failure("Koneksi lagi bermasalah. Cek internet lalu coba lagi.")
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Terjadi kesalahan tak terduga")
    }

    private fun errorMessage(res: Response<*>): String {
        val raw = try {
            res.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        val parsed = try {
            Gson().fromJson(raw, BaseResponse::class.java)?.message
        } catch (e: Exception) {
            null
        }
        return parsed?.takeIf { it.isNotBlank() } ?: "Server menolak permintaan (kode ${res.code()})"
    }
}