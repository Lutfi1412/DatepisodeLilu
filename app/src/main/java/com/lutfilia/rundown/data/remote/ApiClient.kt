package com.lutfilia.rundown.data.remote

import com.google.gson.GsonBuilder
import com.lutfilia.rundown.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /** BuildConfig.API_HOST berasal dari file .env di root project. */
    private val baseUrl: String = BuildConfig.API_HOST.trim().trimEnd('/') + "/"

    // serializeNulls: supaya payload seperti {"rating": null} tetap terkirim persis seperti contoh API.
    private val gson = GsonBuilder().serializeNulls().create()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
