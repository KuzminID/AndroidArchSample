package ru.marwinka.androidarchsample.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class RefreshCharactersUseCaseTest {
    @Test
    fun `delegates to the repository and returns its result`() =
        runTest {
            val repository = FakeCharacterRepository()
            val useCase = RefreshCharactersUseCase(repository)

            val result = useCase()

            assertTrue(result is AppResult.Success)
            assertEquals(1, repository.refreshCallCount)
        }

    @Test
    fun `propagates a failed refresh`() =
        runTest {
            val repository = FakeCharacterRepository()
            val failure = AppResult.Failure(AppError.Unknown(IllegalStateException("network down")))
            repository.refreshResult = failure
            val useCase = RefreshCharactersUseCase(repository)

            val result = useCase()

            assertEquals(failure, result)
        }
}
