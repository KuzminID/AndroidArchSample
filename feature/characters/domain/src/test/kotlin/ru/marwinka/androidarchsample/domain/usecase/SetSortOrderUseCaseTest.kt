package ru.marwinka.androidarchsample.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class SetSortOrderUseCaseTest {
    @Test
    fun `updates the repository's sort order`() =
        runTest {
            val repository = FakeCharacterRepository()
            val useCase = SetSortOrderUseCase(repository)

            useCase(SortOrder.NAME_DESC)

            repository.observeSortOrder().test {
                assertEquals(SortOrder.NAME_DESC, awaitItem())
            }
        }
}
