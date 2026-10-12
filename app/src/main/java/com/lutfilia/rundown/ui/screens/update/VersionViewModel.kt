package com.lutfilia.rundown.ui.screens.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutfilia.rundown.BuildConfig
import com.lutfilia.rundown.Graph
import com.lutfilia.rundown.data.repository.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateInfo(val current: String, val latest: String, val link: String)

/**
 * Membandingkan versi aplikasi (versionName di app/build.gradle.kts) dengan versi dari server.
 * Berjalan di latar belakang sejak aplikasi dibuka. Kalau server lambat atau gagal,
 * aplikasi tetap jalan normal (splash tidak menunggu).
 */
class VersionViewModel : ViewModel() {
    private val repo = Graph.versionRepository

    private val _update = MutableStateFlow<UpdateInfo?>(null)

    /** Tidak null = versi tidak sama, aplikasi perlu diperbarui. */
    val update: StateFlow<UpdateInfo?> = _update.asStateFlow()

    init {
        check()
    }

    private fun check() {
        viewModelScope.launch {
            val current = BuildConfig.VERSION_NAME
            when (val res = repo.getLatestVersion()) {
                is ApiResult.Success -> {
                    val latest = res.data
                    if (normalize(latest.version) != normalize(current)) {
                        _update.value = UpdateInfo(current, latest.version, latest.link)
                    }
                }
                is ApiResult.Failure -> Unit // lambat / gagal: diabaikan
            }
        }
    }

    /** Hanya dipakai kalau server tidak mengirim link: supaya pengguna tidak terkunci. */
    fun dismiss() {
        _update.value = null
    }

    private fun normalize(v: String) = v.trim().removePrefix("v").removePrefix("V")
}