package com.dgopadakak.tagsgallery.add.util

internal fun calculateFinalTagIds(
    selectedCommonTagIds: List<Long>,
    individualAddedTagIds: List<Long>,
    individualRemovedTagIds: List<Long>
): List<Long> {
    return (selectedCommonTagIds + individualAddedTagIds)
        .toSet()
        .minus(individualRemovedTagIds.toSet())
        .toList()
}
