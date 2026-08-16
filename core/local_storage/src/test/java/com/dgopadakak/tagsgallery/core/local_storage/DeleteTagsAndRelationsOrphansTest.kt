package com.dgopadakak.tagsgallery.core.local_storage

import androidx.room.Room
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.room.AppDatabase
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Проверка вычисления осиротевших медиа в [TagDao.deleteTagsAndRelations] - тех, что после
 * удаления тегов остались без единой связи. Именно для них Repository отпускает persisted
 * URI permission, поэтому ошибка в этой выборке означает утечку гранта или потерю доступа
 * к медиа, у которого теги на самом деле остались
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeleteTagsAndRelationsOrphansTest {

    private lateinit var database: AppDatabase
    private lateinit var tagDao: TagDao

    private val firstMediaId = "content://media/external/images/media/1"
    private val secondMediaId = "content://media/external/images/media/2"

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tagDao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `media keeping another tag is not reported as orphaned`() = runBlocking {
        insertTags(FIRST_TAG_ID, SECOND_TAG_ID)
        tagDao.insertMediaTagCrossRefs(
            listOf(
                MediaTagCrossRef(firstMediaId, FIRST_TAG_ID),
                MediaTagCrossRef(firstMediaId, SECOND_TAG_ID)
            )
        )

        val orphaned = tagDao.deleteTagsAndRelations(listOf(FIRST_TAG_ID))

        assertEquals(emptyList<String>(), orphaned)
    }

    @Test
    fun `media losing its only tag is reported as orphaned`() = runBlocking {
        insertTags(FIRST_TAG_ID)
        tagDao.insertMediaTagCrossRefs(
            listOf(MediaTagCrossRef(firstMediaId, FIRST_TAG_ID))
        )

        val orphaned = tagDao.deleteTagsAndRelations(listOf(FIRST_TAG_ID))

        assertEquals(listOf(firstMediaId), orphaned)
    }

    @Test
    fun `only orphaned media is reported when several tags are deleted at once`() = runBlocking {
        insertTags(FIRST_TAG_ID, SECOND_TAG_ID, THIRD_TAG_ID)
        tagDao.insertMediaTagCrossRefs(
            listOf(
                // Останется с третьим тегом
                MediaTagCrossRef(firstMediaId, FIRST_TAG_ID),
                MediaTagCrossRef(firstMediaId, THIRD_TAG_ID),
                // Потеряет оба своих тега
                MediaTagCrossRef(secondMediaId, FIRST_TAG_ID),
                MediaTagCrossRef(secondMediaId, SECOND_TAG_ID)
            )
        )

        val orphaned = tagDao.deleteTagsAndRelations(listOf(FIRST_TAG_ID, SECOND_TAG_ID))

        assertEquals(listOf(secondMediaId), orphaned)
    }

    @Test
    fun `tag without media produces no orphans`() = runBlocking {
        insertTags(FIRST_TAG_ID)

        val orphaned = tagDao.deleteTagsAndRelations(listOf(FIRST_TAG_ID))

        assertEquals(emptyList<String>(), orphaned)
    }

    @Test
    fun `tags themselves are still deleted`() = runBlocking {
        insertTags(FIRST_TAG_ID, SECOND_TAG_ID)
        tagDao.insertMediaTagCrossRefs(
            listOf(MediaTagCrossRef(firstMediaId, FIRST_TAG_ID))
        )

        tagDao.deleteTagsAndRelations(listOf(FIRST_TAG_ID))

        assertNull(tagDao.getTagById(FIRST_TAG_ID))
        assertEquals(SECOND_TAG_ID, tagDao.getTagById(SECOND_TAG_ID)?.id)
    }

    private suspend fun insertTags(vararg tagIds: Long) {
        tagDao.insertAllTags(
            *tagIds
                .map { Tag(id = it, name = "tag $it", color = Tag.Color.NO_COLOR) }
                .toTypedArray()
        )
    }

    private companion object {
        const val FIRST_TAG_ID = 1L
        const val SECOND_TAG_ID = 2L
        const val THIRD_TAG_ID = 3L
    }
}
