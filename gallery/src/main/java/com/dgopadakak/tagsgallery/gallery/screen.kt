package com.dgopadakak.tagsgallery.gallery

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.video.VideoFrameDecoder

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }

    val tagList by viewModel.tags.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        selectedMediaUris = uris
        uris.forEach { uri ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (selectedMediaUris.isEmpty()) {
            Text(
                text = "Select the media to apply the tags to"
            )
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                        )
                    )
                }
            ) {
                Text("Add media")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                selectedMediaUris.forEach { uri ->
                    val imageLoader = ImageLoader.Builder(LocalContext.current)
                        .components {
                            add(VideoFrameDecoder.Factory())
                        }
                        .build()
                    AsyncImage(
                        modifier = Modifier
                            .height(180.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        model = uri,
                        imageLoader = imageLoader,
                        contentDescription = "Media file selected by user"
                    )
                }
            }
            tagList.forEach { tag ->
                FilterChip(
                    selected = selectedTagIds.contains(tag.id),
                    onClick = {
                        selectedTagIds = if (selectedTagIds.contains(tag.id)) {
                            selectedTagIds - tag.id
                        } else {
                            selectedTagIds + tag.id
                        }
                    },
                    label = { Text(tag.name) }
                )
            }
            Row {
                Button(
                    modifier = Modifier
                        .padding(end = 4.dp),
                    onClick = {
                        selectedMediaUris.forEach { uri ->
                            viewModel.saveMediaTags(
                                mediaId = uri.toString(),
                                selectedTagIds = selectedTagIds.toList()
                            )
                        }
                        selectedMediaUris = emptyList()
                        selectedTagIds = emptySet()
                    },
                    enabled = selectedTagIds.isNotEmpty()
                ) {
                    Text("Add")
                }
                Button(
                    modifier = Modifier
                        .padding(start = 4.dp),
                    onClick = {
                        selectedMediaUris = emptyList()
                        selectedTagIds = emptySet()
                    }
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
