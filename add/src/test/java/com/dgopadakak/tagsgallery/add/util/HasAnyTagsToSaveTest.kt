package com.dgopadakak.tagsgallery.add.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasAnyTagsToSaveTest {

    @Test
    fun `returns false when nothing is selected at all`() {
        assertFalse(
            hasAnyTagsToSave(
                selectedCommonTagIds = emptyList(),
                allIndividualAddedTagIds = emptyList()
            )
        )
    }

    @Test
    fun `returns false when individual lists exist but are all empty`() {
        assertFalse(
            hasAnyTagsToSave(
                selectedCommonTagIds = emptyList(),
                allIndividualAddedTagIds = listOf(emptyList<Long>(), emptyList<Long>())
            )
        )
    }

    @Test
    fun `returns true when a common tag is selected`() {
        assertTrue(
            hasAnyTagsToSave(
                selectedCommonTagIds = listOf(1L),
                allIndividualAddedTagIds = emptyList()
            )
        )
    }

    @Test
    fun `returns true when one media has an individual tag`() {
        assertTrue(
            hasAnyTagsToSave(
                selectedCommonTagIds = emptyList(),
                allIndividualAddedTagIds = listOf(emptyList<Long>(), listOf(2L), emptyList<Long>())
            )
        )
    }

    @Test
    fun `returns true when both common and individual tags are selected`() {
        assertTrue(
            hasAnyTagsToSave(
                selectedCommonTagIds = listOf(1L),
                allIndividualAddedTagIds = listOf(listOf(2L))
            )
        )
    }
}
