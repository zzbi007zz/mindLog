package com.example.diary

import android.app.Application
import com.example.diary.data.DiaryRepository
import com.example.diary.data.ImageStorage
import com.example.diary.data.LocalDiaryRepository
import com.example.diary.data.LocalImageStorage
import com.example.diary.data.local.DiaryDatabase

/**
 * Holds the application-scoped singletons (DB, storage, repository) built by
 * hand for the single-module MVP. A future multi-source phase (e.g. Firebase)
 * can introduce Hilt here — documented in the plan.
 */
class DiaryAppContainer(private val context: Application) {
    val database: DiaryDatabase by lazy { DiaryDatabase.get(context) }
    val imageStorage: ImageStorage by lazy { LocalImageStorage(context) }
    val repository: DiaryRepository by lazy {
        LocalDiaryRepository(database.entryDao(), imageStorage)
    }
}

class DiaryApp : Application() {
    lateinit var container: DiaryAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DiaryAppContainer(this)
        // TEMP Phase-3 debug seed — removed in Phase 4/5 when the editor exists.
        Phase3Seed.seedIfEmpty(this, container.repository)
    }
}