package com.dgopadakak.tagsgallery.core.local_storage.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
    val color: Color
) {

    enum class Color(val colorLong: Long?, val compareToken: Int) {
        NO_COLOR(null, 0),
        RED(0xFFFF0000, 1),
        YELLOW(0xFFFFFF00, 2),
        GREEN(0xFF00FF00, 3),
        BLUE(0xFF0000FF, 4)
    }
}
