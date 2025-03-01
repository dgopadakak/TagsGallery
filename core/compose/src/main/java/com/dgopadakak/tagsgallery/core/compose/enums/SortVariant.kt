package com.dgopadakak.tagsgallery.core.compose.enums

enum class SortVariant(private val variantName: String) {
    NAME("Name"),
    DATE("Date"),
    COLOR("Color");

    override fun toString(): String {
        return variantName
    }

    companion object {
        val DEFAULT_SORT_VARIANT = NAME
    }
}
