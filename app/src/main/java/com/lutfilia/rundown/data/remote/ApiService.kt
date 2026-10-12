package com.lutfilia.rundown.data.remote

import com.lutfilia.rundown.data.model.BaseResponse
import com.lutfilia.rundown.data.model.CreateTanggalRequest
import com.lutfilia.rundown.data.model.DokumentasiRequest
import com.lutfilia.rundown.data.model.DokumentasiResponse
import com.lutfilia.rundown.data.model.ErrorDetailResponse
import com.lutfilia.rundown.data.model.RundownDetailResponse
import com.lutfilia.rundown.data.model.RundownRequest
import com.lutfilia.rundown.data.model.TanggalListResponse
import com.lutfilia.rundown.data.model.VersionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Semua endpoint = {host}/{api}. Host dibaca dari .env (lihat app/build.gradle.kts). */
interface ApiService {

    // ----- Versi aplikasi -----
    @GET("get-version")
    suspend fun getVersion(): Response<VersionResponse>

    // ----- Tanggal -----
    @GET("get-tanggal")
    suspend fun getTanggal(): Response<TanggalListResponse>

    @POST("create-tanggal")
    suspend fun createTanggal(@Body body: CreateTanggalRequest): Response<BaseResponse>

    @DELETE("delete-tanggal/{id}")
    suspend fun deleteTanggal(@Path("id") id: Int): Response<BaseResponse>

    // ----- Rundown -----
    @POST("create-rundown/{tanggalId}")
    suspend fun createRundown(
        @Path("tanggalId") tanggalId: Int,
        @Body body: List<RundownRequest>
    ): Response<BaseResponse>

    @GET("get/rundown/{tanggalId}")
    suspend fun getRundown(@Path("tanggalId") tanggalId: Int): Response<RundownDetailResponse>

    @PUT("update-rundown/{rundownId}")
    suspend fun updateRundown(
        @Path("rundownId") rundownId: Int,
        @Body body: RundownRequest
    ): Response<BaseResponse>

    @HTTP(method = "DELETE", path = "delete-data/rundown", hasBody = true)
    suspend fun deleteRundown(@Body ids: List<Int>): Response<BaseResponse>

    // ----- Dokumentasi (satu URL, dua method) -----
    @GET("dokumentasi/{tanggalId}")
    suspend fun getDokumentasi(@Path("tanggalId") tanggalId: Int): Response<DokumentasiResponse>

    @PUT("dokumentasi/{tanggalId}")
    suspend fun putDokumentasi(
        @Path("tanggalId") tanggalId: Int,
        @Body body: DokumentasiRequest
    ): Response<DokumentasiResponse>

    // ----- Error -----
    @GET("get/error/{tanggalId}")
    suspend fun getError(@Path("tanggalId") tanggalId: Int): Response<ErrorDetailResponse>

    @HTTP(method = "DELETE", path = "delete-data/error", hasBody = true)
    suspend fun deleteError(@Body ids: List<Int>): Response<BaseResponse>

    @POST("try-again/{errorId}/{tanggalId}")
    suspend fun tryAgain(
        @Path("errorId") errorId: Int,
        @Path("tanggalId") tanggalId: Int,
        @Body body: RundownRequest
    ): Response<BaseResponse>
}