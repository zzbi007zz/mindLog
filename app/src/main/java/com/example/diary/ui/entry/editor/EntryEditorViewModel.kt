package com.example.diary.ui.entry.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.DiaryRepository
import com.example.diary.data.model.EntryDraft
import com.example.diary.data.model.ImageRef
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val title: String = "",
    val body: String = "",
    val images: List<StagedImage> = emptyList(),
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val hasLoaded: Boolean = false,
    val isEditing: Boolean = false,
) {
    val canSave: Boolean get() = !isSaving
}

sealed interface EditorEvent {
    data object Saved : EditorEvent
}

class EntryEditorViewModel(
    private val repository: DiaryRepository,
    private val entryId: Long?,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    private val _body = MutableStateFlow("")
    private val _images = MutableStateFlow<List<StagedImage>>(emptyList())
    private val _isSaving = MutableStateFlow(false)
    private val _events = Channel<EditorEvent>(Channel.BUFFERED)

    private val isEditing = entryId != null

    // Baseline for the dirty check: empty for create, the loaded entry for edit.
    private var baselineTitle = ""
    private var baselineBody = ""
    private var baselineImages: List<StagedImage> = emptyList()
    private val _hasLoaded = MutableStateFlow(entryId == null) // create mode starts "loaded"

    val events: Flow<EditorEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<EditorUiState> = combine(
        _title, _body, _images, _isSaving, _hasLoaded,
    ) { title, body, images, saving, loaded ->
        val dirty = if (loaded) {
            title != baselineTitle || body != baselineBody || images != baselineImages
        } else {
            false
        }
        EditorUiState(
            title = title,
            body = body,
            images = images,
            isSaving = saving,
            isDirty = dirty,
            hasLoaded = loaded,
            isEditing = isEditing,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EditorUiState())

    init {
        if (entryId != null) loadExisting(entryId)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            val entry = repository.observeEntry(id).first() ?: return@launch
            val stored = entry.images.map { StagedImage.Stored(it.path) as StagedImage }
            baselineTitle = entry.title
            baselineBody = entry.body
            baselineImages = stored
            _title.value = entry.title
            _body.value = entry.body
            _images.value = stored
            _hasLoaded.value = true
        }
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onBodyChange(value: String) { _body.value = value }

    fun onImagesPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _images.value = _images.value + uris.map { StagedImage.New(it) as StagedImage }
    }

    fun onRemoveImage(index: Int) {
        val current = _images.value
        if (index !in current.indices) return
        _images.value = current.toMutableList().also { it.removeAt(index) }
    }

    fun onSave() {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            val draft = EntryDraft(
                id = entryId,
                title = _title.value.trim(),
                body = _body.value,
                images = _images.value.map { img ->
                    when (img) {
                        is StagedImage.Stored -> ImageRef.Stored(img.path)
                        is StagedImage.New -> ImageRef.New(img.uri)
                    }
                },
            )
            repository.upsertEntry(draft)
            _isSaving.value = false
            _events.send(EditorEvent.Saved)
        }
    }
}