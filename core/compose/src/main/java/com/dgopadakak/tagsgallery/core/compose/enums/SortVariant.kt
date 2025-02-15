package com.dgopadakak.tagsgallery.core.compose.enums

enum class SortVariant(private val variantName: String) {
    NAME("Name"),
    DATE("Date");

    override fun toString(): String {
        return variantName
    }
}
