package com.example.diary.data.mapper

import com.example.diary.data.local.EntryImageEntity
import com.example.diary.data.local.EntryWithImages
import com.example.diary.data.model.DiaryEntry
import com.example.diary.data.model.DiaryImage

fun EntryWithImages.toDomain(): DiaryEntry = DiaryEntry(
    id = entry.id,
    title = entry.title,
    body = entry.body,
    createdAt = entry.createdAt,
    updatedAt = entry.updatedAt,
    images = images
        .sortedBy { it.position }
        .map { DiaryImage(id = it.id, path = it.path, position = it.position) },
)

fun DiaryImage.toEntity(entryId: Long): EntryImageEntity = EntryImageEntity(
    id = id,
    entryId = entryId,
    path = path,
    position = position,
)