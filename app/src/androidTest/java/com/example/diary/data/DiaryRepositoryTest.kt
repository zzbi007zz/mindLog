package com.example.diary.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.diary.data.local.DiaryDatabase
import com.example.diary.data.local.EntryEntity
import com.example.diary.data.model.EntryDraft
import com.example.diary.data.model.ImageRef
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiaryRepositoryTest {

    private lateinit var db: DiaryDatabase
    private lateinit var storage: ImageStorage
    private lateinit var repository: DiaryRepository

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, DiaryDatabase::class.java).build()
        storage = LocalImageStorage(context)
        repository = LocalDiaryRepository(db.entryDao(), storage)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeImageFile(content: String): Uri {
        val f = File.createTempFile("diary-test", ".jpg", context.cacheDir)
        f.writeBytes(content.toByteArray())
        return Uri.fromFile(f)
    }

    @Test
    fun importImage_copiesBytesIntoAppStorage() = runBlocking {
        val src = makeImageFile("hello-world")
        val rel = storage.importImage(src)
        assertTrue("relative path under images/", rel.startsWith("images/"))
        val file = storage.fileFor(rel)
        assertTrue("file exists in app storage", file.exists())
        assertEquals("hello-world", file.readText())
    }

    @Test
    fun insertEntry_emitsWithImages_newestFirst() = runBlocking {
        val uriA = makeImageFile("a")
        val uriB = makeImageFile("b")
        val id = repository.upsertEntry(
            EntryDraft(title = "t", body = "b", images = listOf(ImageRef.New(uriA), ImageRef.New(uriB)))
        )

        val entries = repository.observeEntries().first()
        assertEquals(1, entries.size)
        val entry = entries.first()
        assertEquals(id, entry.id)
        assertEquals(2, entry.images.size)
        assertEquals(listOf(0, 1), entry.images.map { it.position })
        assertTrue(storage.fileFor(entry.images[0].path).exists())
        assertTrue(storage.fileFor(entry.images[1].path).exists())

        // Newest-first: a second, older entry sorts after.
        db.entryDao().insertEntry(EntryEntity(title = "old", body = "x", createdAt = 0, updatedAt = 0))
        val ordered = repository.observeEntries().first()
        assertEquals(id, ordered.get(0).id)
        assertTrue(ordered.get(0).createdAt > ordered.get(1).createdAt)
    }

    @Test
    fun updateEntry_removingImage_deletesRowAndFile() = runBlocking {
        val uri1 = makeImageFile("1")
        val uri2 = makeImageFile("2")
        val id = repository.upsertEntry(
            EntryDraft(title = "t", body = "b", images = listOf(ImageRef.New(uri1), ImageRef.New(uri2)))
        )
        val created = repository.observeEntry(id).first()!!
        val keptPath = created.images[0].path
        val removedPath = created.images[1].path

        // Update: keep only the first stored image.
        val updatedId = repository.upsertEntry(
            EntryDraft(
                id = id,
                title = "t2",
                body = "b2",
                images = listOf(ImageRef.Stored(keptPath)),
            )
        )
        assertEquals(id, updatedId)
        val updated = repository.observeEntry(id).first()!!
        assertEquals("t2", updated.title)
        assertEquals(1, updated.images.size)
        assertTrue("kept file survives", storage.fileFor(keptPath).exists())
        assertFalse("removed file deleted", storage.fileFor(removedPath).exists())
    }

    @Test
    fun deleteEntry_removesRowsAndFiles_cascade() = runBlocking {
        val uri = makeImageFile("img")
        val id = repository.upsertEntry(EntryDraft(title = "t", body = "b", images = listOf(ImageRef.New(uri))))
        val entry = repository.observeEntry(id).first()!!
        val path = entry.images[0].path
        assertTrue(storage.fileFor(path).exists())

        repository.deleteEntry(id)

        assertNull(repository.observeEntry(id).first())
        assertEquals(0, repository.observeEntries().first().size)
        assertEquals("image row cascaded", 0, db.entryDao().imagesForEntry(id).size)
        assertFalse("image file deleted", storage.fileFor(path).exists())
    }

    @Test
    fun observeEntry_returnsNull_whenMissing() = runBlocking {
        assertNull(repository.observeEntry(999L).first())
        assertNotNull(repository.observeEntries().first())
    }

    @Test
    fun sweepOrphans_removesUnreferencedButKeepsReferenced() = runBlocking {
        // Create an entry with one referenced image.
        val id = repository.upsertEntry(
            EntryDraft(title = "t", body = "b", images = listOf(ImageRef.New(makeImageFile("keep"))))
        )
        val entry = repository.observeEntry(id).first()!!
        val referenced = entry.images[0].path

        // Drop a stray, unreferenced file directly into images/.
        val stray = File(storage.fileFor(referenced).parentFile, "stray.jpg")
        stray.writeBytes(byteArrayOf(1))
        assertEquals("stored image verifiable", "keep", storage.fileFor(referenced).readText())
        assertTrue("stray written", stray.exists())

        // Sweep with only the referenced set (not the DB-derived set).
        storage.sweepOrphans(setOf(referenced))

        assertTrue("referenced file kept", storage.fileFor(referenced).exists())
        assertFalse("stray file removed", stray.exists())
    }
}