package ru.marwinka.androidarchsample.feature.characters.data.local

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CharacterDaoTest {
    private val database = TestCharacterDatabase.create()
    private val dao = database.characterDao()

    private val rick = CharacterEntity(1, "Rick Sanchez", "Human", "rick.png")
    private val morty = CharacterEntity(2, "Morty Smith", "Human", "morty.png")

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replaceAll drops characters that are gone from the server`() =
        runTest {
            dao.replaceAll(listOf(rick, morty))

            dao.replaceAll(listOf(morty))

            assertEquals(listOf(morty), dao.observeAllSortedByNameAsc().first().map { it.character })
        }

    @Test
    fun `replaceAll emits the new list once, without an empty intermediate state`() =
        runTest {
            dao.replaceAll(listOf(rick))

            dao.observeAllSortedByNameAsc().test {
                assertEquals(listOf(rick), awaitItem().map { it.character })

                dao.replaceAll(listOf(rick, morty))

                assertEquals(listOf(morty, rick), awaitItem().map { it.character })
            }
        }

    @Test
    fun `replaceAll keeps favorites`() =
        runTest {
            dao.replaceAll(listOf(rick))
            dao.toggleFavorite(rick.id)

            dao.replaceAll(listOf(rick, morty))

            val rows = dao.observeAllSortedByNameAsc().first()
            assertEquals(listOf(false, true), rows.map { it.isFavorite })
        }

    @Test
    fun `toggleFavorite adds and then removes the favorite`() =
        runTest {
            dao.replaceAll(listOf(rick))

            dao.toggleFavorite(rick.id)
            assertTrue(dao.observeById(rick.id).first()!!.isFavorite)

            dao.toggleFavorite(rick.id)
            assertFalse(dao.observeById(rick.id).first()!!.isFavorite)
        }

    @Test
    fun `sorting by name works in both directions`() =
        runTest {
            dao.replaceAll(listOf(rick, morty))

            assertEquals(listOf(morty, rick), dao.observeAllSortedByNameAsc().first().map { it.character })
            assertEquals(listOf(rick, morty), dao.observeAllSortedByNameDesc().first().map { it.character })
        }
}
