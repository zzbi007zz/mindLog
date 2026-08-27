package com.example.diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diary.DiaryAppContainer
import com.example.diary.data.DiaryRepository
import com.example.diary.ui.entry.detail.EntryDetailViewModel
import com.example.diary.ui.entry.list.EntryListViewModel

/**
 * Supplies ViewModels with the repository from the app container.
 * Enables [androidx.lifecycle.viewmodel.compose.viewModel] with an explicit
 * factory lambda, keeping the small manual-DI story intact (no Hilt in MVP).
 */
object ViewModelFactory {

    fun list(container: DiaryAppContainer): ViewModelProviderFactory<EntryListViewModel> =
        factory { EntryListViewModel(container.repository) }

    fun detail(container: DiaryAppContainer, entryId: Long): ViewModelProviderFactory<EntryDetailViewModel> =
        factory { EntryDetailViewModel(container.repository, entryId) }

    private fun <VM : ViewModel> factory(create: () -> VM): ViewModelProviderFactory<VM> =
        ViewModelProviderFactory(create)
}

/** Small wrapper so callers read `ViewModelFactory.list(container)` cleanly. */
class ViewModelProviderFactory<VM : ViewModel>(
    private val create: () -> VM,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}