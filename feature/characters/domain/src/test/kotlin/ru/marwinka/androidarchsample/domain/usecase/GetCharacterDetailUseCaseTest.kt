package ru.marwinka.androidarchsample.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class GetCharacterDetailUseCaseTest {
    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)

    @Test
    fun `emits the matching character`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val useCase = GetCharacterDetailUseCase(repository)

            useCase(rick.id).test {
                assertEquals(rick, awaitItem())
            }
        }

    @Test
    fun `emits null when no character matches the id`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val useCase = GetCharacterDetailUseCase(repository)

            useCase(999).test {
                assertNull(awaitItem())
            }
        }
}
