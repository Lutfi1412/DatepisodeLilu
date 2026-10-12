package com.lutfilia.rundown.ui.screens.adddate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutfilia.rundown.Graph
import com.lutfilia.rundown.data.repository.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface AddDateEvent {
    data class Created(val id: Int, val date: LocalDate) : AddDateEvent
    data class Failed(val message: String) : AddDateEvent
}

class AddDateViewModel : ViewModel() {
    private val repo = Graph.repository

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = MutableSharedFlow<AddDateEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<AddDateEvent> = _events.asSharedFlow()

    fun create(date: LocalDate, label: String) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            when (val res = repo.createTanggal(date, label)) {
                is ApiResult.Success -> _events.tryEmit(AddDateEvent.Created(res.data, date))
                is ApiResult.Failure -> {
                    _busy.value = false
                    _events.tryEmit(AddDateEvent.Failed(res.message))
                }
            }
        }
    }
}