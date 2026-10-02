package ru.marwinka.androidarchsample.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.network.CharacterApi
import ru.marwinka.androidarchsample.core.network.model.CharacterDto
import ru.marwinka.androidarchsample.core.network.model.CharacterListInfoDto
import ru.marwinka.androidarchsample.core.network.model.CharacterListResponseDto
import ru.marwinka.androidarchsample.core.preferences.UserPreferences
import ru.marwinka.androidarchsample.data.local.CharacterDao
import ru.marwinka.androidarchsample.data.local.CharacterEntity
import ru.marwinka.androidarchsample.domain.model.SortOrder

class CharacterRepositoryImplTest {
    private val api = mockk<CharacterApi>()
    private val dao = mockk<CharacterDao>()
    private val preferences = mockk<UserPreferences>()
    private val dispatchers =
        object : DispatcherProvider {
            override val io = Dispatchers.Unconfined
            override val default = Dispatchers.Unconfined
            override val main = Dispatchers.Unconfined
        }

    private val repository = CharacterRepositoryImpl(api, dao, preferences, dispatchers)

    @Test
    fun `observeCharacters marks cached entities as favorite using preferences`() =
        runTest {
            every { dao.observeAllSortedByNameAsc() } returns
                flowOf(
                    listOf(CharacterEntity(1, "Rick Sanchez", "Human", "rick.png")),
                )
            every { preferences.observeFavoriteIds() } returns flowOf(setOf("1"))

            repository.observeCharacters(SortOrder.NAME_ASC).test {
                val result = awaitItem()
                assertEquals(1, result.size)
                assertTrue(result.first().isFavorite)
                awaitComplete()
            }
        }

    @Test
    fun `refresh persists mapped characters and returns success`() =
        runTest {
            coEvery { api.getCharacters(1) } returns
                CharacterListResponseDto(
                    info = CharacterListInfoDto(next = null),
                    results = listOf(CharacterDto(1, "Rick Sanchez", "Human", "rick.png")),
                )
            coEvery { dao.replaceAll(any()) } returns Unit

            val result = repository.refresh()

            assertTrue(result is AppResult.Success)
            coVerify { dao.replaceAll(match { it.size == 1 && it.first().id == 1 }) }
        }

    @Test
    fun `refresh follows pagination until the last page`() =
        runTest {
            coEvery { api.getCharacters(1) } returns
                CharacterListResponseDto(
                    info = CharacterListInfoDto(next = "https://rickandmortyapi.com/api/character?page=2"),
                    results = listOf(CharacterDto(1, "Rick Sanchez", "Human", "rick.png")),
                )
            coEvery { api.getCharacters(2) } returns
                CharacterListResponseDto(
                    info = CharacterListInfoDto(next = null),
                    results = listOf(CharacterDto(2, "Morty Smith", "Human", "morty.png")),
                )
            coEvery { dao.replaceAll(any()) } returns Unit

            val result = repository.refresh()

            assertTrue(result is AppResult.Success)
            coVerify { dao.replaceAll(match { it.size == 2 }) }
        }

    @Test
    fun `refresh maps a network failure to AppError Network instead of throwing`() =
        runTest {
            val failure = java.io.IOException("no network")
            coEvery { api.getCharacters(1) } throws failure

            val result = repository.refresh()

            assertEquals(AppResult.Failure(AppError.Network(failure)), result)
        }

    @Test
    fun `observeCharacter marks the cached entity as favorite using preferences`() =
        runTest {
            every { dao.observeById(1) } returns flowOf(CharacterEntity(1, "Rick Sanchez", "Human", "rick.png"))
            every { preferences.observeFavoriteIds() } returns flowOf(setOf("1"))

            repository.observeCharacter(1).test {
                assertTrue(awaitItem()!!.isFavorite)
                awaitComplete()
            }
        }

    @Test
    fun `observeCharacter emits null when nothing is cached for the id`() =
        runTest {
            every { dao.observeById(1) } returns flowOf(null)
            every { preferences.observeFavoriteIds() } returns flowOf(emptySet())

            repository.observeCharacter(1).test {
                assertNull(awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `toggleFavorite delegates to preferences using the character id as a string`() =
        runTest {
            coEvery { preferences.toggleFavorite("42") } returns Unit

            repository.toggleFavorite(42)

            coVerify { preferences.toggleFavorite("42") }
        }

    @Test
    fun `setSortOrder persists the order's name to preferences`() =
        runTest {
            coEvery { preferences.setSortOrder("NAME_DESC") } returns Unit

            repository.setSortOrder(SortOrder.NAME_DESC)

            coVerify { preferences.setSortOrder("NAME_DESC") }
        }

    @Test
    fun `observeSortOrder falls back to NAME_ASC for an unrecognized stored value`() =
        runTest {
            every { preferences.observeSortOrder() } returns flowOf("not_a_real_order")

            repository.observeSortOrder().test {
                assertEquals(SortOrder.NAME_ASC, awaitItem())
                awaitComplete()
            }
        }
}
