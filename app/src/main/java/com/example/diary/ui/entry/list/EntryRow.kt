package com.example.diary.ui.entry.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.diary.data.ImageStorage
import com.example.diary.data.model.DiaryEntry
import com.example.diary.ui.common.timeLabel
import com.example.diary.ui.entry.ImageThumbnail

/** One list row: optional thumbnail, title/body preview, entry time. */
@Composable
fun EntryRow(
    entry: DiaryEntry,
    imageStorage: ImageStorage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailPath = entry.images.minByOrNull { it.position }?.path

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (thumbnailPath != null) {
            ImageThumbnail(
                imageStorage = imageStorage,
                relativePath = thumbnailPath,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.size(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = timeLabel(entry.createdAt),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}