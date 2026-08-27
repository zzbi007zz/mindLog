package com.example.diary

import android.graphics.Bitmap
import android.graphics.Color
import android.content.Context
import android.net.Uri
import com.example.diary.data.DiaryRepository
import com.example.diary.data.model.EntryDraft
import com.example.diary.data.model.ImageRef
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * TEMPORARY Phase-3 debug seed — removed in Phase 4/5 when the editor lets
 * users create entries for real. Seeds a few entries with generated images so
 * the list/detail UI can be verified before the editor exists.
 */
object Phase3Seed {

    fun seedIfEmpty(context: Context, repository: DiaryRepository) {
        runBlocking {
            if (repository.observeEntries().first().isNotEmpty()) return@runBlocking

            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val drafts = listOf(
                Triple("First entry", "A quiet note about the day.", Color.rgb(200, 60, 60)),
                Triple("Second entry", "Coffee, sky, a walk.", Color.rgb(60, 120, 200)),
                Triple("Third entry", "Plans for tomorrow.", Color.rgb(70, 160, 110)),
            )
            drafts.forEachIndexed { index, (title, body, color) ->
                val uris = (0 until (index % 3)).map { writePng(context, imagesDir, color) }
                repository.upsertEntry(
                    EntryDraft(
                        title = title,
                        body = body,
                        images = uris.map { ImageRef.New(it) },
                    )
                )
            }
        }
    }

    private fun writePng(context: Context, dir: File, color: Int): Uri {
        val bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        val f = File(dir, "seed-${System.nanoTime()}.png")
        FileOutputStream(f).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return Uri.fromFile(f)
    }
}