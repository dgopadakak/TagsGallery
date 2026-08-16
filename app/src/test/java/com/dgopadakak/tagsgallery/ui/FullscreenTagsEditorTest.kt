package com.dgopadakak.tagsgallery.ui

import android.app.Application
import androidx.compose.ui.geometry.Rect
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import com.dgopadakak.tagsgallery.core.local_storage.Repository
import com.dgopadakak.tagsgallery.core.local_storage.models.MediaTagCrossRef
import com.dgopadakak.tagsgallery.core.local_storage.models.Tag
import com.dgopadakak.tagsgallery.core.local_storage.preferences.PreferencesRepository
import com.dgopadakak.tagsgallery.core.local_storage.room.AppDatabase
import com.dgopadakak.tagsgallery.core.local_storage.room.TagDao
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
 * Редактирование списка тегов открытого в просмотрщике медиа: [MainViewModel] держит состояние
 * редактора, а список медиа просмотрщика остается снимком, сделанным при открытии.
 *
 * Тесты работают с настоящим Room в памяти - фейка репозитория в проекте нет, а поведение Room
 * (и его Flow) здесь как раз часть проверяемого. Все исполнители Room переведены на тестовый
 * диспетчер, поэтому advanceUntilIdle доводит до конца и запросы, и рассылку инвалидаций
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
// Голый Application вместо App: иначе Robolectric поднимает Hilt-граф, а с ним и
// DatabaseInitializer на Dispatchers.IO, заводящий настоящие tags_gallery_db и settings
// параллельно каждому тесту
@Config(sdk = [34], application = Application::class)
class FullscreenTagsEditorTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: AppDatabase
    private lateinit var gatedTagDao: GatedTagDao
    private lateinit var repository: Repository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        database = Room
            .inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .allowMainThreadQueries()
            .build()

        gatedTagDao = GatedTagDao(database.tagDao())

        repository = Repository(
            tagDao = gatedTagDao,
            preferencesRepository = PreferencesRepository(UnusedDataStore),
            dispatcher = testDispatcher
        )

        viewModel = MainViewModel(repository, RuntimeEnvironment.getApplication())
    }

    @After
    fun tearDown() {
        // Сначала гасим коллекторы вьюмодели: иначе подписка на getAllTags() переживает
        // базу и остается висеть на закрытом InvalidationTracker
        viewModel.viewModelScope.cancel()
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `opening editor loads tag ids already linked to the media`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID, FRIENDS_TAG_ID))

        assertEquals(
            setOf(SUMMER_TAG_ID, FRIENDS_TAG_ID),
            viewModel.uiState.value.tagsEditorState?.selectedTagIds
        )
    }

    @Test
    fun `editor does not open for media whose viewer was closed while the query ran`() =
        runTest(testDispatcher) {
            insertTags()
            database.tagDao().insertMediaTagCrossRefs(
                listOf(MediaTagCrossRef(FIRST_MEDIA_ID, SUMMER_TAG_ID))
            )
            viewModel.setFullscreenContent(fullscreenContent())

            viewModel.openTagsEditor(FIRST_MEDIA_ID.toUri())
            viewModel.setFullscreenContent(null)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.tagsEditorState)
        }

    /**
     * Просмотрщик закрыли с недошедшим запросом и открыли заново на другом медиа той же
     * подборки: запрос надо гасить при закрытии, иначе редактор всплывет для медиа,
     * которое пользователь уже не смотрит
     */
    @Test
    fun `request from a closed viewer does not open the editor after reopening`() =
        runTest(testDispatcher) {
            insertTags()
            database.tagDao().insertMediaTagCrossRefs(
                listOf(
                    MediaTagCrossRef(FIRST_MEDIA_ID, SUMMER_TAG_ID),
                    MediaTagCrossRef(SECOND_MEDIA_ID, SEA_TAG_ID)
                )
            )
            viewModel.setFullscreenContent(fullscreenContent())

            val firstMediaGate = CompletableDeferred<Unit>()
            gatedTagDao.tagIdsGates[FIRST_MEDIA_ID] = firstMediaGate

            viewModel.openTagsEditor(FIRST_MEDIA_ID.toUri())
            advanceUntilIdle()
            viewModel.setFullscreenContent(null)
            viewModel.setFullscreenContent(fullscreenContent())
            firstMediaGate.complete(Unit)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.tagsEditorState)
        }

    /**
     * Тап Edit, свайп на соседнюю страницу и повторный тап Edit, пока первый запрос в пути:
     * оба медиа лежат в одном просмотрщике, так что проверки "медиа все еще в подборке" мало
     */
    @Test
    fun `late answer of a superseded request does not replace the editor`() =
        runTest(testDispatcher) {
            insertTags()
            database.tagDao().insertMediaTagCrossRefs(
                listOf(
                    MediaTagCrossRef(FIRST_MEDIA_ID, SUMMER_TAG_ID),
                    MediaTagCrossRef(SECOND_MEDIA_ID, SEA_TAG_ID)
                )
            )
            viewModel.setFullscreenContent(fullscreenContent())

            val firstMediaGate = CompletableDeferred<Unit>()
            gatedTagDao.tagIdsGates[FIRST_MEDIA_ID] = firstMediaGate

            viewModel.openTagsEditor(FIRST_MEDIA_ID.toUri())
            advanceUntilIdle()
            viewModel.openTagsEditor(SECOND_MEDIA_ID.toUri())
            advanceUntilIdle()
            firstMediaGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(SECOND_MEDIA_ID.toUri(), viewModel.uiState.value.tagsEditorState?.mediaUri)
            assertEquals(setOf(SEA_TAG_ID), viewModel.uiState.value.tagsEditorState?.selectedTagIds)
        }

    @Test
    fun `toggling an unlinked tag adds it to the editor selection`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(SEA_TAG_ID)

        assertEquals(
            setOf(SUMMER_TAG_ID, SEA_TAG_ID),
            viewModel.uiState.value.tagsEditorState?.selectedTagIds
        )
    }

    @Test
    fun `toggling a selected tag removes it from the editor selection`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID, FRIENDS_TAG_ID))

        viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)

        assertEquals(
            setOf(FRIENDS_TAG_ID),
            viewModel.uiState.value.tagsEditorState?.selectedTagIds
        )
    }

    @Test
    fun `saving rewrites cross refs of the edited media and leaves other media alone`() =
        runTest(testDispatcher) {
            openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

            viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)
            viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)
            viewModel.saveTagsEditor()
            advanceUntilIdle()

            assertEquals(
                listOf(FRIENDS_TAG_ID),
                database.tagDao().getTagIdsForMedia(FIRST_MEDIA_ID)
            )
            assertEquals(
                listOf(SEA_TAG_ID),
                database.tagDao().getTagIdsForMedia(SECOND_MEDIA_ID)
            )
        }

    /**
     * Перезапись теми же связями дернула бы инвалидацию Room, а на ней галерея прогоняет
     * uriExists() по всему списку медиа - слишком дорого за сохранение без изменений
     */
    @Test
    fun `saving an unchanged selection does not rewrite cross refs`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        val mediaIdEmissions = mutableListOf<List<String>>()
        val collectJob = launch { database.tagDao().getAllMediaIds().collect { mediaIdEmissions += it } }
        advanceUntilIdle()

        viewModel.saveTagsEditor()
        advanceUntilIdle()
        collectJob.cancel()

        assertEquals(1, mediaIdEmissions.size)
    }

    /**
     * Запись успевает завершиться уже после того, как редактор закрыли и открыли на другом
     * медиа: гасить чужой редактор нельзя, вместе с ним пропал бы несохраненный выбор
     */
    @Test
    fun `late write does not close an editor opened for another media`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))
        viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)

        val writeGate = CompletableDeferred<Unit>()
        gatedTagDao.crossRefsWriteGate = writeGate

        viewModel.saveTagsEditor()
        advanceUntilIdle()
        viewModel.openTagsEditor(SECOND_MEDIA_ID.toUri())
        advanceUntilIdle()
        writeGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(SECOND_MEDIA_ID.toUri(), viewModel.uiState.value.tagsEditorState?.mediaUri)
    }

    @Test
    fun `saving closes the editor`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)
        viewModel.saveTagsEditor()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.tagsEditorState)
    }

    @Test
    fun `saving an empty selection keeps cross refs untouched`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)
        viewModel.saveTagsEditor()
        advanceUntilIdle()

        assertEquals(
            listOf(SUMMER_TAG_ID),
            database.tagDao().getTagIdsForMedia(FIRST_MEDIA_ID)
        )
    }

    @Test
    fun `saving an empty selection asks to confirm media removal`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)
        viewModel.saveTagsEditor()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.tagsEditorState?.confirmingRemoval)
    }

    @Test
    fun `dismissing the removal confirmation returns to the editor without touching cross refs`() =
        runTest(testDispatcher) {
            openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID, FRIENDS_TAG_ID))

            viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)
            viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)
            viewModel.saveTagsEditor()
            advanceUntilIdle()
            viewModel.dismissTagsEditorRemoval()

            assertEquals(false, viewModel.uiState.value.tagsEditorState?.confirmingRemoval)
            assertEquals(FIRST_MEDIA_ID.toUri(), viewModel.uiState.value.tagsEditorState?.mediaUri)
            assertEquals(
                setOf(SUMMER_TAG_ID, FRIENDS_TAG_ID),
                database.tagDao().getTagIdsForMedia(FIRST_MEDIA_ID).toSet()
            )
        }

    @Test
    fun `tags are exposed to the editor sorted by name`() = runTest(testDispatcher) {
        insertTags()
        advanceUntilIdle()

        assertEquals(
            listOf("Friends", "Sea", "Summer"),
            viewModel.allTags.value.map { it.name }
        )
    }

    /**
     * Требование задачи: правка тегов не должна менять список медиа просмотрщика, иначе открытое
     * медиа исчезало бы из-под пальца, перестав удовлетворять фильтрам галереи. Тест зеленый с
     * первого запуска - он сторожит снимок, сделанный при открытии, от переезда на живой Flow
     */
    @Test
    fun `editing tags leaves the cached fullscreen media list untouched`() = runTest(testDispatcher) {
        val content = openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(SUMMER_TAG_ID)
        viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)
        viewModel.saveTagsEditor()
        advanceUntilIdle()

        assertEquals(content, viewModel.uiState.value.fullscreenContent)
    }

    @Test
    fun `closing fullscreen content closes the editor`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.setFullscreenContent(null)

        assertNull(viewModel.uiState.value.tagsEditorState)
    }

    @Test
    fun `dismissing the editor drops unsaved changes`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID))

        viewModel.toggleTagsEditorTag(FRIENDS_TAG_ID)
        viewModel.dismissTagsEditor()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.tagsEditorState)
        assertEquals(
            listOf(SUMMER_TAG_ID),
            database.tagDao().getTagIdsForMedia(FIRST_MEDIA_ID)
        )
    }

    /**
     * У MediaTagCrossRef нет внешних ключей, поэтому мертвый id в выборе редактора привел бы к
     * связи с несуществующим тегом
     */
    @Test
    fun `editor selection drops a tag deleted on another screen`() = runTest(testDispatcher) {
        openEditorForFirstMedia(linkedTagIds = listOf(SUMMER_TAG_ID, FRIENDS_TAG_ID))

        repository.deleteTagsAndRelations(
            tagIds = listOf(SUMMER_TAG_ID),
            contentResolver = RuntimeEnvironment.getApplication().contentResolver
        )
        advanceUntilIdle()

        assertEquals(
            setOf(FRIENDS_TAG_ID),
            viewModel.uiState.value.tagsEditorState?.selectedTagIds
        )
    }

    /**
     * Открывает просмотрщик и в нем редактор для первого медиа. Второе медиа с тегом Sea есть
     * в базе всегда - это контрольные связи, которых правки первого медиа касаться не должны
     */
    private suspend fun TestScope.openEditorForFirstMedia(
        linkedTagIds: List<Long>
    ): FullscreenContentModel {
        insertTags()
        database.tagDao().insertMediaTagCrossRefs(
            linkedTagIds.map { MediaTagCrossRef(FIRST_MEDIA_ID, it) } +
                MediaTagCrossRef(SECOND_MEDIA_ID, SEA_TAG_ID)
        )

        val content = fullscreenContent()
        viewModel.setFullscreenContent(content)
        viewModel.openTagsEditor(FIRST_MEDIA_ID.toUri())
        advanceUntilIdle()
        return content
    }

    private fun fullscreenContent() = FullscreenContentModel(
        startIndex = 0,
        uris = listOf(FIRST_MEDIA_ID.toUri(), SECOND_MEDIA_ID.toUri()),
        placeholderImgRequests = emptyList(),
        startAnimationCoordinates = Rect.Zero
    )

    private suspend fun insertTags() {
        database.tagDao().insertAllTags(
            Tag(id = SUMMER_TAG_ID, name = "Summer", color = Tag.Color.NO_COLOR),
            Tag(id = FRIENDS_TAG_ID, name = "Friends", color = Tag.Color.RED),
            Tag(id = SEA_TAG_ID, name = "Sea", color = Tag.Color.BLUE)
        )
    }

    /**
     * DAO, у которого можно придержать ответ конкретного запроса. Без этого гонки двух
     * пересекающихся правок не воспроизвести: тестовый диспетчер однопоточный и отдает ответы
     * строго в порядке вызовов, а на устройстве запросы уходят в пул IO и возвращаются как
     * придется
     */
    private class GatedTagDao(private val delegate: TagDao) : TagDao by delegate {

        val tagIdsGates = mutableMapOf<String, CompletableDeferred<Unit>>()
        var crossRefsWriteGate: CompletableDeferred<Unit>? = null

        override suspend fun getTagIdsForMedia(mediaId: String): List<Long> {
            tagIdsGates[mediaId]?.await()
            return delegate.getTagIdsForMedia(mediaId)
        }

        override suspend fun deleteAndInsertMediaTagCrossRefs(
            mediaIdsToDeleteCrossRefs: Set<String>,
            crossRefsToAdd: List<MediaTagCrossRef>
        ) {
            crossRefsWriteGate?.await()
            delegate.deleteAndInsertMediaTagCrossRefs(
                mediaIdsToDeleteCrossRefs = mediaIdsToDeleteCrossRefs,
                crossRefsToAdd = crossRefsToAdd
            )
        }
    }

    /**
     * Настройки нужны конструктору [Repository], но в этих тестах не используются. Заглушка
     * падает при любом обращении, чтобы молчаливая зависимость от нее не осталась незамеченной
     */
    private object UnusedDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences>
            get() = error("Настройки в FullscreenTagsEditorTest не используются")

        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences = error("Настройки в FullscreenTagsEditorTest не используются")
    }

    private companion object {
        const val FIRST_MEDIA_ID = "content://media/external/images/media/1"
        const val SECOND_MEDIA_ID = "content://media/external/images/media/2"

        const val SUMMER_TAG_ID = 1L
        const val FRIENDS_TAG_ID = 2L
        const val SEA_TAG_ID = 3L
    }
}
