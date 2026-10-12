package com.lutfilia.rundown.ui.screens.rundownform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutfilia.rundown.Graph
import com.lutfilia.rundown.data.model.RundownDraft
import com.lutfilia.rundown.data.repository.ApiResult
import com.lutfilia.rundown.util.parseTanggal
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface CreateEvent {
    data object Done : CreateEvent
    data class Error(val message: String) : CreateEvent
}

class CreateRundownViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repo = Graph.repository

    val tanggalId: Int = savedStateHandle.get<Int>("tanggalId") ?: 0
    val date: LocalDate? = parseTanggal(savedStateHandle.get<String>("date"))

    private val _staged = MutableStateFlow<List<RundownDraft>>(emptyList())
    val staged: StateFlow<List<RundownDraft>> = _staged.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _events = MutableSharedFlow<CreateEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CreateEvent> = _events.asSharedFlow()

    fun stage(draft: RundownDraft) = _staged.update { it + draft }

    fun unstage(index: Int) = _staged.update { list -> list.filterIndexed { i, _ -> i != index } }

    /**
     * Kirim semua rundown (yang sudah ditumpuk + yang sedang diisi).
     * Server memproses lewat worker, jadi layar loading dijamin tampil 1 detik
     * lalu lanjut ke detail tanggal walaupun server masih bekerja. Request dijalankan di
     * scope aplikasi supaya tidak ikut batal saat layar ini ditutup.
     */
    fun submit(extra: RundownDraft?) {
        val all = _staged.value + listOfNotNull(extra)
        if (all.isEmpty() || _sending.value) return
        _sending.value = true
        viewModelScope.launch {
            val request = Graph.appScope.async { repo.createRundown(tanggalId, all) }
            delay(1000)
            val result = if (request.isCompleted) request.await() else null
            if (result is ApiResult.Failure) {
                _sending.value = false
                _events.tryEmit(CreateEvent.Error(result.message))
            } else {
                _events.tryEmit(CreateEvent.Done)
            }
        }
    }
}
