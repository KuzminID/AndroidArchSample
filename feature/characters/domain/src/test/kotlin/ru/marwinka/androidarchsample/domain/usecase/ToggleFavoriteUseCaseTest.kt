package ru.marwinka.androidarchsample.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class ToggleFavoriteUseCaseTest {
    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)

    @Test
    fun `toggling flips the favorite flag of the matching character`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val useCase = ToggleFavoriteUseCase(repository)

            useCase(rick.id)

            repository.observeCharacter(rick.id).test {
                assertTrue(awaitItem()!!.isFavorite)
            }
        }
}
