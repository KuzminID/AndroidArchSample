package ru.marwinka.androidarchsample.feature.characters.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.testing.FakeCharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.testing.FakeCharacterSettingsRepository

class ObserveCharactersUseCaseTest {
    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)
    private val morty = Character(2, "Morty Smith", "Human", "morty.png", isFavorite = false)

    private val repository = FakeCharacterRepository(listOf(rick, morty))
    private val settings = FakeCharacterSettingsRepository()
    private val useCase = ObserveCharactersUseCase(repository, settings)

    @Test
    fun `emits characters in the stored sort order`() =
        runTest {
            useCase().test {
                assertEquals(listOf(morty, rick), awaitItem())
            }
        }

    @Test
    fun `re-emits when the sort order changes`() =
        runTest {
            useCase().test {
                assertEquals(listOf(morty, rick), awaitItem())

                settings.setSortOrder(SortOrder.NAME_DESC)

                assertEquals(listOf(rick, morty), awaitItem())
            }
        }

    @Test
    fun `re-emits when the cache changes`() =
        runTest {
            useCase().test {
                awaitItem()

                repository.characters.value = listOf(rick)

                assertEquals(listOf(rick), awaitItem())
            }
        }
}
