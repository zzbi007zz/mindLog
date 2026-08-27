package com.example.diary.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Owns the on-device image files under app-private `filesDir/images/`. */
interface ImageStorage {
    /** Copies a picked image into app storage, returns its relative path. */
    suspend fun importImage(source: Uri): String

    /** Deletes the file at a relative path; no-op if missing. */
    suspend fun deleteImage(relativePath: String)

    /** Resolves a relative path to a [File] (for Coil loading). */
    fun fileFor(relativePath: String): File
}

class LocalImageStorage(private val context: Context) : ImageStorage {

    override suspend fun importImage(source: Uri): String = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, IMAGES_SUBDIR).apply { mkdirs() }
        val relative = "$IMAGES_SUBDIR/${UUID.randomUUID()}.jpg"
        val dest = File(imagesDir, relative.removePrefix("$IMAGES_SUBDIR/"))

        val input = try {
            context.contentResolver.openInputStream(source)
        } catch (_: Exception) {
            // Some callers pass file:// Uris that the resolver rejects.
            source.scheme?.let { s -> if (s == "file") FileInputStream(source.path!!) else null }
        } ?: error("Cannot open image source: $source")

        input.use { ins ->
            dest.outputStream().use { out -> ins.copyTo(out, bufferSize = 64 * 1024) }
        }
        relative
    }

    override suspend fun deleteImage(relativePath: String) = withContext(Dispatchers.IO) {
        val f = fileFor(relativePath)
        if (f.exists()) f.delete()
    }

    override fun fileFor(relativePath: String): File = File(context.filesDir, relativePath)

    private companion object {
        const val IMAGES_SUBDIR = "images"
    }
}