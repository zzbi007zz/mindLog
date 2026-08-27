package com.example.diary.ui.entry.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.DiaryRepository
import com.example.diary.data.model.DiaryEntry
import com.example.diary.ui.common.dateOf
import com.example.diary.ui.common.headerLabel
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** A date-grouped section of entries, newest section first. */
data class EntrySection(
    val date: String,
    val entries: List<DiaryEntry>,
)

sealed interface EntryListUiState {
    data object Loading : EntryListUiState
    data object Empty : EntryListUiState
    data class Data(val sections: List<EntrySection>) : EntryListUiState
}

class EntryListViewModel(repository: DiaryRepository) : ViewModel() {

    val uiState: StateFlow<EntryListUiState> = repository
        .observeEntries()
        .map { entries ->
            if (entries.isEmpty()) {
                EntryListUiState.Empty
            } else {
                val sections = buildList {
                    var currentDate: LocalDate? = null
                    var currentLabel: String? = null
                    var currentItems = mutableListOf<DiaryEntry>()
                    for (e in entries) {
                        val date = dateOf(e.createdAt)
                        val label = headerLabel(date)
                        if (date != currentDate) {
                            if (currentLabel != null) {
                                add(EntrySection(currentLabel!!, currentItems))
                            }
                            currentDate = date
                            currentLabel = label
                            currentItems = mutableListOf()
                        }
                        currentItems.add(e)
                    }
                    if (currentLabel != null) {
                        add(EntrySection(currentLabel!!, currentItems))
                    }
                }
                EntryListUiState.Data(sections)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryListUiState.Loading)
}