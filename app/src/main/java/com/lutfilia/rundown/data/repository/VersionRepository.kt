package com.lutfilia.rundown.data.repository

import com.lutfilia.rundown.data.model.LatestVersion
import com.lutfilia.rundown.data.remote.ApiService
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Cek versi aplikasi terbaru ke server: GET {host}/get-version. */
class VersionRepository(private val api: ApiService) {

    /** Versi + link pembaruan terbaru dari server. Gagal -> Failure (pemanggil cukup mengabaikan). */
    suspend fun getLatestVersion(): ApiResult<LatestVersion> = try {
        val res = api.getVersion()
        val body = res.body()
        val version = body?.data?.version?.trim().orEmpty()
        when {
            !res.isSuccessful -> ApiResult.Failure("Server menjawab ${res.code()}")
            body?.status == false -> ApiResult.Failure(body.message ?: "Permintaan ditolak server")
            version.isEmpty() -> ApiResult.Failure("Versi dari server kosong")
            else -> ApiResult.Success(LatestVersion(version, body?.data?.link?.trim().orEmpty()))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        ApiResult.Failure("Tidak ada koneksi")
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Gagal cek versi")
    }
}