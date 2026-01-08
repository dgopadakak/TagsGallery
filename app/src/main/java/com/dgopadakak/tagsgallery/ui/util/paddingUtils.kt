package com.dgopadakak.tagsgallery.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Механизм учета размера только открытого NavigationBar
 */
@Composable
fun NavBarPaddingEditor(
    navBarPadding: MutableState<PaddingValues>,
    controlsVisible: MutableState<Boolean>
) {
    val inset = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(inset.calculateBottomPadding()) {
        if (controlsVisible.value && inset.calculateBottomPadding() > navBarPadding.value.calculateBottomPadding())
            navBarPadding.value = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = inset.calculateBottomPadding()
            )
    }
    LaunchedEffect(inset.calculateLeftPadding(layoutDirection)) {
        if (controlsVisible.value && inset.calculateLeftPadding(layoutDirection) > navBarPadding.value.calculateLeftPadding(layoutDirection))
            navBarPadding.value = PaddingValues(
                start = inset.calculateLeftPadding(layoutDirection),
                top = 0.dp,
                end = 0.dp,
                bottom = 0.dp
            )
    }
    LaunchedEffect(inset.calculateRightPadding(layoutDirection)) {
        if (controlsVisible.value && inset.calculateRightPadding(layoutDirection) > navBarPadding.value.calculateRightPadding(layoutDirection))
            navBarPadding.value = PaddingValues(
                start = 0.dp,
                top = 0.dp,
                end = inset.calculateRightPadding(layoutDirection),
                bottom = 0.dp
            )
    }
}

// TODO: постараться объединить StatusBarPaddingEditor с NavBarPaddingEditor
/**
 * Механизм учета размера только открытого StatusBar
 */
@Composable
fun StatusBarPaddingEditor(
    statusBarPadding: MutableState<PaddingValues>,
    controlsVisible: MutableState<Boolean>
) {
    val inset = WindowInsets.statusBars.asPaddingValues()

    LaunchedEffect(inset.calculateTopPadding()) {
        if (controlsVisible.value && inset.calculateTopPadding() > statusBarPadding.value.calculateTopPadding()) {
            statusBarPadding.value = PaddingValues(
                start = 0.dp,
                top = inset.calculateTopPadding(),
                end = 0.dp,
                bottom = 0.dp
            )
        }
    }
}
