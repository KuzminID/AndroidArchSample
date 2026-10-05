package ru.marwinka.androidarchsample.feature.characters.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.testing.RecordingLogger
import ru.marwinka.androidarchsample.core.testing.TestDispatcherProvider
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterEntity
import ru.marwinka.androidarchsample.feature.characters.data.local.TestCharacterDatabase
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterDto
import ru.marwinka.androidarchsample.feature.characters.data.remote.FakeCharacterApi
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class CharacterRepositoryImplTest {
    private val database = TestCharacterDatabase.create()
    private val dao = database.characterDao()
    private val logger = RecordingLogger()

    private val rick = CharacterDto(1, "Rick Sanchez", "Human", "rick.png")
    private val morty = CharacterDto(2, "Morty Smith", "Human", "morty.png")

    private fun TestScope.repository(api: FakeCharacterApi) =
        CharacterRepositoryImpl(api, dao, TestDispatcherProvider(StandardTestDispatcher(testScheduler)), logger)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `refresh loads every page and replaces the cache`() =
        runTest {
            dao.replaceAll(listOf(CharacterEntity(99, "Deleted on server", "Alien", "x.png")))
            val api = FakeCharacterApi(pages = listOf(listOf(rick), listOf(morty)))

            val result = repository(api).refresh()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf(1, 2), api.requestedPages)
            assertEquals(listOf(2, 1), dao.observeAllSortedByNameAsc().first().map { it.character.id })
        }

    @Test
    fun `network failure keeps the cache and returns Network`() =
        runTest {
            dao.replaceAll(listOf(CharacterEntity(1, "Rick Sanchez", "Human", "rick.png")))
            val failure = IOException("offline")
            val api = FakeCharacterApi().apply { this.failure = failure }

            val result = repository(api).refresh()

            // Coroutines may copy the exception to recover the stack trace, so compare type and message.
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.Network)
            assertEquals(failure.message, error.cause?.message)
            assertEquals(1, dao.observeAllSortedByNameAsc().first().size)
            assertTrue(logger.errors.isEmpty())
        }

    @Test
    fun `unexpected failure returns Unknown and is logged`() =
        runTest {
            val failure = HttpException(Response.error<Unit>(500, "".toResponseBody(null)))
            val api = FakeCharacterApi().apply { this.failure = failure }

            val result = repository(api).refresh()

            assertTrue((result as AppResult.Failure).error is AppError.Unknown)
            assertTrue(logger.errors.single().second is HttpException)
        }

    @Test
    fun `cancellation is rethrown, not turned into a result`() =
        runTest {
            val api = FakeCharacterApi().apply { failure = CancellationException("cancelled") }

            val outcome = runCatching { repository(api).refresh() }

            assertTrue(outcome.exceptionOrNull() is CancellationException)
        }

    @Test
    fun `observeCharacters maps rows and follows the sort order`() =
        runTest {
            val repository = repository(FakeCharacterApi(pages = listOf(listOf(rick, morty))))
            repository.refresh()
            repository.toggleFavorite(rick.id)

            repository.observeCharacters(SortOrder.NAME_DESC).test {
                val characters = awaitItem()
                assertEquals(listOf("Rick Sanchez", "Morty Smith"), characters.map { it.name })
                assertEquals(listOf(true, false), characters.map { it.isFavorite })
            }
        }

    @Test
    fun `toggleFavorite is reflected by observeCharacter`() =
        runTest {
            val repository = repository(FakeCharacterApi(pages = listOf(listOf(rick))))
            repository.refresh()

            repository.observeCharacter(rick.id).test {
                assertEquals(false, awaitItem()!!.isFavorite)

                assertEquals(AppResult.Success(Unit), repository.toggleFavorite(rick.id))

                assertEquals(true, awaitItem()!!.isFavorite)
            }
        }

    @Test
    fun `observeCharacter emits null for an unknown id`() =
        runTest {
            assertNull(repository(FakeCharacterApi()).observeCharacter(42).first())
        }
}
