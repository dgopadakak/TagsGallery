package com.dgopadakak.tagsgallery.core.local_storage.room.models

import androidx.room.Embedded
import androidx.room.Relation

data class TagWithMedia(
    @Embedded
    val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId"
    )
    val media: List<MediaTagCrossRef> // Assuming mediaId references media
)
