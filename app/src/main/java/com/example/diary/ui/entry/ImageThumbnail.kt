package com.example.diary.ui.entry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.diary.data.ImageStorage

/**
 * Coil-backed image that resolves a stored relative path via [ImageStorage].
 * Centralized so every image decode goes through the same path → File mapping
 * (Coil needs a File/URI, not a bare relative string).
 */
@Composable
fun ImageThumbnail(
    imageStorage: ImageStorage,
    relativePath: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val file = imageStorage.fileFor(relativePath)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
        )
    }
}