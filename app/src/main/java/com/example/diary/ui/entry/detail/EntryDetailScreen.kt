package com.example.diary.ui.entry.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Edit
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
import com.example.diary.ui.common.fullDateLabel
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
    val date = entry?.let { fullDateLabel(it.createdAt) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is EntryDetailEvent.Deleted) onChange()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                date?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = e.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (e.body.isNotEmpty()) {
                    Text(
                        text = e.body,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                if (e.images.isNotEmpty()) {
                    LazyRow(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(e.images, key = { it.id }) { image ->
                            ImageThumbnail(
                                imageStorage = imageStorage,
                                relativePath = image.path,
                                modifier = Modifier.width(176.dp).height(176.dp),
                                cornerRadius = 16.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}