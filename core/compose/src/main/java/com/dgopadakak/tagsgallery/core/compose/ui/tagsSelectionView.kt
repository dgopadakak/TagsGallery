package com.dgopadakak.tagsgallery.core.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgopadakak.tagsgallery.core.compose.enums.SortVariant
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsSelectionView(
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

        FlowRow(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            tags.forEach { tag ->
                FilterChip(     // TODO: подкрасить
                    modifier = Modifier.padding(horizontal = 4.dp),
                    selected = selectedTagsIds.contains(tag.id),
                    onClick = { onTagClick(tag.id) },
                    label = { Text(tag.name) }      // FIXME: не обрезает текст в теге
                )
            }
        }
    }
}
