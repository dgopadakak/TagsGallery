package com.dgopadakak.tagsgallery.core.local_storage.room.models

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

    enum class Color(val colorLong: Long?) {
        NO_COLOR(null),
        RED(0xFFFF0000),
        YELLOW(0xFFFFFF00),
        GREEN(0xFF00FF00),
        BLUE(0xFF0000FF)
    }
}
