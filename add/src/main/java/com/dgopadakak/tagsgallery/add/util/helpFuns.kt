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

internal fun hasAnyTagsToSave(
    selectedCommonTagIds: List<Long>,
    allIndividualAddedTagIds: Collection<List<Long>>
): Boolean {
    return selectedCommonTagIds.isNotEmpty() || allIndividualAddedTagIds.any { it.isNotEmpty() }
}
