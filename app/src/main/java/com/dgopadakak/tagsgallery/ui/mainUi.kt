package com.dgopadakak.tagsgallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgopadakak.tagsgallery.navigation.LocalWindowSizeClass
import com.dgopadakak.tagsgallery.navigation.Routes

@Composable
internal fun MainScreen(
    windowSizeClass: WindowSizeClass,
    windowInsetsControllerCompat: WindowInsetsControllerCompat,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    // TODO: при расширении на другие платформы - сделать больше, чем 2 варианта UI навигации
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass
    ) {
        // Box - правильный контейнер для накладываемых друг на друга Composable. Это первый уровень
        // иерархии Composable, так что без него происходят артефакты вроде залипания FullScreenMediaView
        Box {
            Scaffold(
                // Явно surface, а не дефолтный для Scaffold background: Gallery построен на
                // BottomSheetScaffold, у которого дефолт - surface, и в динамической палитре
                // эти две роли уже не совпадают. Без этого фон Tags и Add отличался бы от Gallery
                containerColor = MaterialTheme.colorScheme.surface,
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(navController)
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(navController)
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Routes.TAGS.route
                    ) {
                        composable(route = Routes.TAGS.route) { Routes.TAGS.ScreenForRoute() }
                        composable(route = Routes.ADD.route) { Routes.ADD.ScreenForRoute() }
                        composable(route = Routes.GALLERY.route) { Routes.GALLERY.ScreenForRoute { viewModel.setFullscreenContent(it) } }
                    }
                }
            }

            uiState.fullscreenContent?.let { content ->
                val removeMediaFromApp: (Uri) -> Unit = { uri ->
                    windowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars())
                    viewModel.deleteMedia(uri)
                    viewModel.setFullscreenContent(null)
                    // TODO: вместо закрытия продумать и протестить поведение при удалении не последнего медиа, последнего медиа (и так же под фильтром)
                }

                FullScreenMediaView(
                    contentModel = content,
                    windowInsetsControllerCompat = windowInsetsControllerCompat,
                    alreadyAnimated = uiState.fullscreenAnimated,
                    volumeKeyEvents = viewModel.volumeKeyEvents,
                    isMuted = uiState.isMuted,
                    onAnimated = { viewModel.setAnimated(true) },
                    onSetMuted = { viewModel.setMuted(it) },
                    onEditTags = { viewModel.openTagsEditor(it) },
                    onDeleteMedia = removeMediaFromApp,
                    onClose = {
                        windowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars())
                        viewModel.setFullscreenContent(null)
                    }
                )

                uiState.tagsEditor?.let { editor ->
                    if (editor.confirmingRemoval) {
                        // Снятый до последнего список тегов равносилен удалению медиа из
                        // приложения, поэтому спрашиваем тем же диалогом, что и кнопка удаления
                        DeleteMediaConfirmDialog(
                            onConfirm = { removeMediaFromApp(editor.mediaUri) },
                            onDismiss = viewModel::dismissTagsEditorRemoval
                        )
                    } else {
                        EditTagsDialog(
                            tags = uiState.allTags,
                            selectedTagIds = editor.selectedTagIds,
                            onTagClick = viewModel::toggleTagsEditorTag,
                            onSave = viewModel::saveTagsEditor,
                            onDismiss = viewModel::dismissTagsEditor
                        )
                    }
                }
            }
        }
    }
}
