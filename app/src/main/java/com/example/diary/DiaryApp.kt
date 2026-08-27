package com.example.diary

import android.app.Application
import com.example.diary.data.DiaryRepository
import com.example.diary.data.ImageStorage
import com.example.diary.data.LocalDiaryRepository
import com.example.diary.data.LocalImageStorage
import com.example.diary.data.local.DiaryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Holds the application-scoped singletons (DB, storage, repository) built by
 * hand for the single-module MVP. A future multi-source phase (e.g. Firebase)
 * can introduce Hilt here — documented in the plan.
 */
class DiaryAppContainer(private val context: Application) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: DiaryDatabase by lazy { DiaryDatabase.get(context) }
    val imageStorage: ImageStorage by lazy { LocalImageStorage(context) }
    val repository: DiaryRepository by lazy {
        LocalDiaryRepository(database.entryDao(), imageStorage)
    }

    /**
     * One-shot startup orphan sweep: removes any file under `images/` not
     * referenced by an entry row. Runs once, before the user can edit, so it
     * cannot race an in-flight import during a session.
     */
    fun sweepImageOrphans() {
        appScope.launch {
            val referenced = database.entryDao().allImagePaths().toSet()
            imageStorage.sweepOrphans(referenced)
        }
    }
}

class DiaryApp : Application() {
    lateinit var container: DiaryAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DiaryAppContainer(this)
        container.sweepImageOrphans()
    }
}