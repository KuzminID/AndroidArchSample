package ru.marwinka.androidarchsample.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class GetCharactersUseCaseTest {
    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)
    private val morty = Character(2, "Morty Smith", "Human", "morty.png", isFavorite = false)

    @Test
    fun `emits characters sorted by current sort order`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(morty, rick))
            val useCase = GetCharactersUseCase(repository)

            useCase().test {
                assertEquals(listOf(morty, rick), awaitItem())
            }
        }

    @Test
    fun `re-emits when sort order changes`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(morty, rick))
            val useCase = GetCharactersUseCase(repository)

            useCase().test {
                assertEquals(listOf(morty, rick), awaitItem())

                repository.setSortOrder(SortOrder.NAME_DESC)

                assertEquals(listOf(rick, morty), awaitItem())
            }
        }
}
