package com.dgopadakak.tagsgallery.core.compose.enums

import androidx.annotation.StringRes
import com.dgopadakak.tagsgallery.core.compose.R

enum class SortVariant(@field:StringRes val labelRes: Int) {
    NAME(R.string.sort_variant_name),
    DATE(R.string.sort_variant_date),
    COLOR(R.string.sort_variant_color);

    companion object {
        val DEFAULT_SORT_VARIANT = NAME
    }
}
