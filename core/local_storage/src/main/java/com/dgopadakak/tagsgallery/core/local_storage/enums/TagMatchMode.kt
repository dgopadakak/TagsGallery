package com.dgopadakak.tagsgallery.core.local_storage.enums

import androidx.annotation.StringRes
import com.dgopadakak.tagsgallery.core.local_storage.R

enum class TagMatchMode(@field:StringRes val labelRes: Int) {
    ALL(R.string.tag_match_mode_all),
    ANY(R.string.tag_match_mode_any),
    EXCLUDE(R.string.tag_match_mode_exclude);

    companion object {
        val DEFAULT_TAG_MATCH_MODE = ALL
    }
}
