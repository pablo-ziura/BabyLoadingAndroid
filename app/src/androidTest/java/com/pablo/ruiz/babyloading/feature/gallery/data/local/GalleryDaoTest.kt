package com.pablo.ruiz.babyloading.feature.gallery.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryDaoTest {
    private lateinit var database: BabyLoadingDatabase
    private lateinit var dao: GalleryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BabyLoadingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.galleryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun itemsAreOrderedNewestFirstAndCanBeDeleted() = runTest {
        val older = entity(id = "older", capturedAt = 100)
        val newer = entity(id = "newer", capturedAt = 200)

        dao.insert(older)
        dao.insert(newer)

        assertEquals(listOf(newer, older), dao.observeItems().first())
        assertEquals(newer, dao.itemById("newer"))

        dao.deleteById("newer")
        assertNull(dao.itemById("newer"))
    }

    private fun entity(id: String, capturedAt: Long) = GalleryItemEntity(
        id = id,
        privateFileName = "$id.jpg",
        capturedAtEpochMillis = capturedAt,
        source = GallerySource.Imported.name,
        pregnancyWeek = null,
    )
}
