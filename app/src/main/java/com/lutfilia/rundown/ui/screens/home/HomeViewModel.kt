package com.lutfilia.rundown.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lutfilia.rundown.Graph
import com.lutfilia.rundown.data.model.DateItem
import com.lutfilia.rundown.data.repository.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    /** null = belum pernah termuat */
    val items: List<DateItem>? = null,
    val refreshing: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val repo = Graph.repository

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var job: Job? = null

    fun refresh() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _state.update { it.copy(refreshing = it.items != null, error = null) }
            when (val res = repo.getTanggal()) {
                is ApiResult.Success ->
                    _state.update { it.copy(items = res.data, refreshing = false, error = null) }
                is ApiResult.Failure -> {
                    val hadData = _state.value.items != null
                    _state.update { it.copy(refreshing = false, error = if (hadData) null else res.message) }
                    if (hadData) _events.tryEmit(res.message)
                }
            }
        }
    }

    fun delete(item: DateItem) {
        viewModelScope.launch {
            when (val res = repo.deleteTanggal(item.id)) {
                is ApiResult.Success -> {
                    _state.update { s -> s.copy(items = s.items?.filterNot { it.id == item.id }) }
                    _events.tryEmit("Tanggal dihapus")
                    refresh()
                }
                is ApiResult.Failure -> _events.tryEmit(res.message)
            }
        }
    }
}
