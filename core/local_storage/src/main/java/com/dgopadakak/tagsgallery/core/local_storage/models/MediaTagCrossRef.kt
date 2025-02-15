package com.dgopadakak.tagsgallery.core.local_storage.models

import androidx.room.Entity

@Entity(primaryKeys = ["mediaId", "tagId"])
data class MediaTagCrossRef(
    val mediaId: String,
    val tagId: Long
)
