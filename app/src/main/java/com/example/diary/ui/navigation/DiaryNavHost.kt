package com.example.diary.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diary.DiaryApp
import com.example.diary.data.ImageStorage
import com.example.diary.ui.ViewModelFactory
import com.example.diary.ui.entry.detail.EntryDetailScreen
import com.example.diary.ui.entry.detail.EntryDetailViewModel
import com.example.diary.ui.entry.list.EntryListScreen
import com.example.diary.ui.entry.list.EntryListViewModel

/** Route constants — stable across phases. */
object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{entryId}"
    const val EDITOR = "editor?entryId={entryId}"

    fun detail(entryId: Long) = "detail/$entryId"
    fun editor(entryId: Long? = null) = if (entryId == null) "editor" else "editor?entryId=$entryId"

    val EDITOR_ARG = "entryId"
}

@Composable
fun DiaryNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val imageStorage = imageStorage(context)

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            val vm: EntryListViewModel = viewModel(factory = ViewModelFactory.list(appContainer(context)))
            EntryListScreen(
                viewModel = vm,
                imageStorage = imageStorage,
                onEntryClick = { id -> navController.navigate(Routes.detail(id)) },
                onCreateClick = { navController.navigate(Routes.editor()) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments!!.getLong("entryId")
            val vm: EntryDetailViewModel = viewModel(
                key = "detail-$entryId",
                factory = ViewModelFactory.detail(appContainer(context), entryId),
            )
            EntryDetailScreen(
                viewModel = vm,
                imageStorage = imageStorage,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.editor(id)) },
                onChange = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument(Routes.EDITOR_ARG) {
                                                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
                ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(Routes.EDITOR_ARG)?.toLongOrNull()
            // Phase 4 implements the real editor. Placeholder keeps nav sane.
            Scaffold { innerPadding ->
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (entryId == null) "Editor — New" else "Editor — Edit $entryId")
                }
            }
        }
    }
}

@Composable
private fun appContainer(context: Context) =
    (context.applicationContext as DiaryApp).container

@Composable
private fun imageStorage(context: Context): ImageStorage =
    appContainer(context).imageStorage