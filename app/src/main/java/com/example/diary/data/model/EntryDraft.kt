package com.example.diary.data.model

import android.net.Uri

/**
 * A reference to an image in a draft: either one already stored in app
 * storage (an existing path) or a freshly-picked [Uri] to import on save.
 */
sealed interface ImageRef {
    data class Stored(val path: String) : ImageRef
    data class New(val uri: Uri) : ImageRef
}

/** A pending create (id == null) or update (id set) of an entry. */
data class EntryDraft(
    val id: Long? = null,
    val title: String = "",
    val body: String = "",
    val images: List<ImageRef> = emptyList(),
)