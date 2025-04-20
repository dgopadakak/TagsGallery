package com.dgopadakak.tagsgallery.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.video.VideoFrameDecoder

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    // TODO: Implement
    TestMediaViewer(viewModel)  // TODO: для теста! Удалить.
}

@Composable
private fun TestMediaViewer(viewModel: SearchViewModel) {   // TODO: для теста! Удалить.
    val uiState by viewModel.searchUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        uiState.tags.forEach {
            Button(onClick = { viewModel.loadMediaForTag(it.id) }) { Text(it.name) }
        }
        Text(text = "Saved Media", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            uiState.foundedMediaUris.forEach { uri ->
                val imageLoader = ImageLoader.Builder(LocalContext.current)
                    .components {
                        add(VideoFrameDecoder.Factory())
                    }
                    .build()
                AsyncImage(
                    model = uri,
                    imageLoader = imageLoader,
                    contentDescription = "Saved media preview",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}
