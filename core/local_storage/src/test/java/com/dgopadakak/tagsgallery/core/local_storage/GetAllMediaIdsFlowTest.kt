package com.dgopadakak.tagsgallery.core.local_storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Проверка допущения, на котором держится синхронизация состояния экрана Add: переиздает ли
 * Flow из [Repository.getAllMediaIds] значение при удалении связей из MediaTagCrossRef.
 *
 * Логика фильтрации в AddViewModel тестами не покрыта - здесь проверяется поведение Room
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetAllMediaIdsFlowTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: Repository

    private val firstMediaId = "content://media/external/images/media/1"
    private val secondMediaId = "content://media/external/images/media/2"

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = Repository(
            tagDao = database.tagDao(),
            preferencesRepository = PreferencesRepository(UnusedDataStore),
            dispatcher = Dispatchers.IO
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `flow re-emits without media id after its cross refs are deleted`() = runBlocking {
        database.tagDao().insertMediaTagCrossRefs(
            listOf(
                MediaTagCrossRef(firstMediaId, TAG_ID),
                MediaTagCrossRef(secondMediaId, TAG_ID)
            )
        )

        val emissions = Channel<List<String>>(Channel.UNLIMITED)
        val collectJob: Job = launch(Dispatchers.IO) {
            repository.getAllMediaIds().collect { emissions.send(it) }
        }

        val beforeDeletion = withTimeout(TIMEOUT_MILLIS) { emissions.receive() }
        assertEquals(setOf(firstMediaId, secondMediaId), beforeDeletion.toSet())

        database.tagDao().deleteMediaTagCrossRefsByMediaIds(listOf(firstMediaId))

        val afterDeletion = withTimeout(TIMEOUT_MILLIS) { emissions.receive() }
        assertEquals(setOf(secondMediaId), afterDeletion.toSet())

        collectJob.cancel()
    }

    @Test
    fun `flow re-emits empty list after all cross refs are deleted`() = runBlocking {
        database.tagDao().insertMediaTagCrossRefs(
            listOf(MediaTagCrossRef(firstMediaId, TAG_ID))
        )

        val emissions = Channel<List<String>>(Channel.UNLIMITED)
        val collectJob: Job = launch(Dispatchers.IO) {
            repository.getAllMediaIds().collect { emissions.send(it) }
        }

        val beforeDeletion = withTimeout(TIMEOUT_MILLIS) { emissions.receive() }
        assertEquals(setOf(firstMediaId), beforeDeletion.toSet())

        database.tagDao().deleteMediaTagCrossRefsByMediaIds(listOf(firstMediaId))

        val afterDeletion = withTimeout(TIMEOUT_MILLIS) { emissions.receive() }
        assertEquals(emptyList<String>(), afterDeletion)

        collectJob.cancel()
    }

    /**
     * Настройки нужны конструктору [Repository], но в этом тесте не используются. Заглушка
     * падает при любом обращении, чтобы молчаливая зависимость от нее не осталась незамеченной
     */
    private object UnusedDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences>
            get() = error("Настройки в GetAllMediaIdsFlowTest не используются")

        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences = error("Настройки в GetAllMediaIdsFlowTest не используются")
    }

    private companion object {
        const val TAG_ID = 1L
        const val TIMEOUT_MILLIS = 10_000L
    }
}
