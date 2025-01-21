package com.dgopadakak.tagsgallery.tags

enum class SortVariant(private val variantName: String) {
    NAME("Name"),
    DATE("Date");

    override fun toString(): String {
        return variantName
    }
}
