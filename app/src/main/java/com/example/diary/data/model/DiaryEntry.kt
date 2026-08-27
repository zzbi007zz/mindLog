package com.example.diary.data.model

import android.net.Uri

/** A gallery image. `path` is app-relative (e.g. "images/uuid.jpg"). */
data class DiaryImage(
    val id: Long,
    val path: String,
    val position: Int,
)

/** A diary entry with its ordered image gallery. */
data class DiaryEntry(
    val id: Long,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val images: List<DiaryImage> = emptyList(),
)