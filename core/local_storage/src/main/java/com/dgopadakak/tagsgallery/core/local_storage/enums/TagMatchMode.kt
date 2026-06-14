package com.dgopadakak.tagsgallery.core.local_storage.enums

enum class TagMatchMode(private val variantName: String) {
    ALL("All"),
    ANY("Any"),
    EXCLUDE("Exclude");

    override fun toString(): String {
        return variantName
    }

    companion object {
        val DEFAULT_TAG_MATCH_MODE = ALL
    }
}
