package com.example.diary.ui.entry.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diary.data.ImageStorage
import com.example.diary.ui.common.timeLabel
import com.example.diary.ui.entry.ImageThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    viewModel: EntryDetailViewModel,
    imageStorage: ImageStorage,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry by viewModel.entry.collectAsState()
    val time = entry?.let { timeLabel(it.createdAt) }

    // One-shot delete completion → navigate back.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is EntryDetailEvent.Deleted) onChange()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(entry?.title?.ifEmpty { "Untitled" } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = { onEdit(entry!!.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        val e = entry
        if (e == null) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Deleted or missing entry",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = entry?.title ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                )
                time?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (e.body.isNotEmpty()) {
                    Text(
                        text = e.body,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                if (e.images.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(e.images, key = { it.id }) { image ->
                            ImageThumbnail(
                                imageStorage = imageStorage,
                                relativePath = image.path,
                                modifier = Modifier.width(160.dp).height(160.dp),
                                cornerRadius = 12.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}