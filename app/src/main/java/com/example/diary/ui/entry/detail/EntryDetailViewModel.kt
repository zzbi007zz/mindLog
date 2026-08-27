package com.example.diary.ui.entry.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.DiaryRepository
import com.example.diary.data.model.DiaryEntry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EntryDetailEvent {
    data object Deleted : EntryDetailEvent
}

class EntryDetailViewModel(
    private val repository: DiaryRepository,
    entryId: Long,
) : ViewModel() {

    val entry: StateFlow<DiaryEntry?> = repository
        .observeEntry(entryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<EntryDetailEvent>(Channel.BUFFERED)
    val events: Flow<EntryDetailEvent> = _events.receiveAsFlow()

    fun delete() {
        val current = entry.value ?: return
        viewModelScope.launch {
            repository.deleteEntry(current.id)
            _events.send(EntryDetailEvent.Deleted)
        }
    }
}