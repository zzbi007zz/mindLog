package com.example.diary.ui.entry.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/** A tappable journal card: thumbnail, serif title, body preview, time. */
@Composable
fun EntryRow(
    entry: DiaryEntry,
    imageStorage: ImageStorage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailPath = entry.images.minByOrNull { it.position }?.path

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (thumbnailPath != null) {
                ImageThumbnail(
                    imageStorage = imageStorage,
                    relativePath = thumbnailPath,
                    modifier = Modifier.size(64.dp),
                    cornerRadius = 14.dp,
                )
                Spacer(Modifier.size(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.size(2.dp))
                if (entry.body.isNotEmpty()) {
                    Text(
                        text = entry.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    text = timeLabel(entry.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}