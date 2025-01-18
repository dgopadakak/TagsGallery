package com.dgopadakak.tagsgallery.core.local_storage.room.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagWithMedia(
    @Embedded
    val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "mediaId",
        associateBy = Junction(MediaTagCrossRef::class)
    )
    val media: List<Long> // Assuming mediaId references media
)
