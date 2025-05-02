package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

// TODO: сделать отдельный ViewModel для этой view, чтоб не нагружать sortBy и другими параметрами
//  стейты и viewModel в местах использования (а надо ли? Ведь придется как-то получать список тегов)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullTagsSelectionView(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    selectedTagsIds: List<Long>,
    onTagClick: (Long) -> Unit,
    sortBy: SortVariant,
    onSortVariantChanged: (SortVariant) -> Unit,
    filterBy: Tag.Color?,
    onFilterVariantChanged: (Tag.Color?) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        SortVariantsRow(
            sortBy = sortBy,
            onSortVariantChanged = onSortVariantChanged
        )

        ColorFilterRow(
            filterBy = filterBy,
            onFilterVariantChanged = onFilterVariantChanged
        )

        SimpleTagsSelectionView(
            tags = tags,
            selectedTagsIds = selectedTagsIds,
            onTagClick = onTagClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleTagsSelectionView(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    selectedTagsIds: List<Long>,
    onTagClick: (Long) -> Unit
) {
    if (tags.isNotEmpty()) {
        FlowRow(
            modifier = modifier
                .verticalScroll(rememberScrollState())
        ) {
            tags.forEach { tag ->
                val baseSelectedColor = MaterialTheme.colorScheme.secondaryContainer
                val tagSelectedColor = tag.color.colorLong?.let { Color(it) } ?: baseSelectedColor
                val blendedSelectedColor = lerp(baseSelectedColor, tagSelectedColor, 0.2f)

                val baseUnselectedColor = MaterialTheme.colorScheme.surface
                val tagUnselectedColor = tag.color.colorLong?.let { Color(it) } ?: baseUnselectedColor
                val blendedUnselectedColor = lerp(baseUnselectedColor, tagUnselectedColor, 0.2f)

                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    selected = selectedTagsIds.contains(tag.id),
                    onClick = { onTagClick(tag.id) },
                    leadingIcon = {
                        if (selectedTagsIds.contains(tag.id)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tag.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = blendedUnselectedColor,
                        selectedContainerColor = blendedSelectedColor
                    )
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("You need to add tags on Tags screen to use them here")
        }
    }
}
