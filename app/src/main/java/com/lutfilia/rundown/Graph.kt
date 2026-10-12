package com.lutfilia.rundown

import android.content.Context
import com.lutfilia.rundown.data.remote.ApiClient
import com.lutfilia.rundown.data.remote.MapsRatingScraper
import com.lutfilia.rundown.data.repository.RundownRepository
import com.lutfilia.rundown.data.repository.VersionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.getValue

/** Penyedia dependensi sederhana (tanpa Hilt, biar proyek ringan). */
object Graph {
    /** Scope yang hidup selama aplikasi: dipakai untuk request yang tidak boleh batal saat pindah layar. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var appContext: Context

    /** Dipanggil dari RundownApp.onCreate(). */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val versionRepository: VersionRepository by lazy { VersionRepository(ApiClient.service) }

    val repository: RundownRepository by lazy {
        RundownRepository(ApiClient.service, MapsRatingScraper(appContext))
    }
}