package com.example.diary.ui.entry.editor

import android.net.Uri

/**
 * An image staged in the editor before save: either one already stored in app
 * storage (existing entry) or a freshly-picked [Uri] not yet imported. The
 * ViewModel maps these to [com.example.diary.data.model.ImageRef] on save —
 * the Phase-2 contract, reused, never forked.
 */
sealed interface StagedImage {
    data class Stored(val path: String) : StagedImage
    data class New(val uri: Uri) : StagedImage
}