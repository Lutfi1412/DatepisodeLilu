package com.lutfilia.rundown.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutfilia.rundown.Graph
import com.lutfilia.rundown.data.model.DateDetail
import com.lutfilia.rundown.data.model.ErrorDetail
import com.lutfilia.rundown.data.model.RundownDraft
import com.lutfilia.rundown.data.repository.ApiResult
import com.lutfilia.rundown.data.repository.getOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val detail: DateDetail? = null,
    val errors: ErrorDetail? = null,
    val failure: String? = null,
    val busy: Boolean = false,
    /** null = belum termuat; "" = belum ada dokumentasi; selain itu = link. */
    val dokumentasi: String? = null,
    val savingDoc: Boolean = false
)

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repo = Graph.repository

    val tanggalId: Int = savedStateHandle.get<Int>("tanggalId") ?: 0
    private val fresh: Boolean = savedStateHandle.get<Boolean>("fresh") ?: false

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var pollJob: Job? = null

    init {
        // Baru selesai kirim rundown: aplikasi masih scraping rating & server memproses lewat worker,
        // jadi cek berkali-kali (12 x 2,5 detik).
        refresh(polls = if (fresh) 12 else 0)
    }

    /** polls = berapa kali lagi mengecek ulang tiap 2,5 detik (untuk menunggu worker server). */
    fun refresh(polls: Int = 0, silent: Boolean = false) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            _state.update { it.copy(refreshing = !silent && it.detail != null, loading = it.detail == null) }
            repeat(polls + 1) { attempt ->
                if (attempt > 0) delay(2500)
                fetchOnce()
            }
            _state.update { it.copy(refreshing = false) }
        }
    }

    private suspend fun fetchOnce() = coroutineScope {
        val rundown = async { repo.getRundown(tanggalId) }
        val errors = async { repo.getError(tanggalId) }
        val doc = async { repo.getDokumentasi(tanggalId) }
        val r = rundown.await()
        val e = errors.await()
        val d = doc.await()
        _state.update { s ->
            s.copy(
                loading = false,
                detail = r.getOrNull() ?: s.detail,
                errors = e.getOrNull() ?: s.errors,
                dokumentasi = d.getOrNull() ?: s.dokumentasi,
                failure = if (r is ApiResult.Failure && s.detail == null) r.message else null
            )
        }
    }

    fun update(rundownId: Int, draft: RundownDraft) =
        mutate("Perubahan dikirim. Kalau ditolak server, cek tab Gagal.") {
            repo.updateRundown(rundownId, draft)
        }

    fun deleteRundowns(ids: List<Int>) = mutate("Rundown dihapus") { repo.deleteRundown(ids) }

    fun deleteErrors(ids: List<Int>) = mutate("Data gagal dihapus") { repo.deleteErrors(ids) }

    fun retry(errorId: Int, draft: RundownDraft) =
        mutate("Dikirim ulang. Hasilnya muncul sebentar lagi.") {
            repo.tryAgain(errorId, tanggalId, draft)
        }

    /** Simpan link dokumentasi (kosong = hapus). Hasil dari server langsung dipakai tanpa refresh penuh. */
    fun saveDokumentasi(link: String) {
        if (_state.value.savingDoc) return
        viewModelScope.launch {
            _state.update { it.copy(savingDoc = true) }
            when (val res = repo.updateDokumentasi(tanggalId, link)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(savingDoc = false, dokumentasi = res.data) }
                    _events.tryEmit(if (res.data.isBlank()) "Dokumentasi dikosongkan" else "Dokumentasi tersimpan")
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(savingDoc = false) }
                    _events.tryEmit(res.message)
                }
            }
        }
    }

    private fun mutate(successMessage: String, block: suspend () -> ApiResult<*>) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            when (val res = block()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(busy = false) }
                    _events.tryEmit(successMessage)
                    refresh(polls = 2, silent = true)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(busy = false) }
                    _events.tryEmit(res.message)
                }
            }
        }
    }
}