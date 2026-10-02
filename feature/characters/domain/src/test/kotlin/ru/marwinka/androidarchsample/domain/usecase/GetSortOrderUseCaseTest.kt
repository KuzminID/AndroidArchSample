package ru.marwinka.androidarchsample.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository

class GetSortOrderUseCaseTest {
    @Test
    fun `emits the repository's current sort order`() =
        runTest {
            val repository = FakeCharacterRepository()
            val useCase = GetSortOrderUseCase(repository)

            useCase().test {
                assertEquals(SortOrder.NAME_ASC, awaitItem())
            }
        }

    @Test
    fun `re-emits after setSortOrder changes it`() =
        runTest {
            val repository = FakeCharacterRepository()
            val useCase = GetSortOrderUseCase(repository)

            useCase().test {
                assertEquals(SortOrder.NAME_ASC, awaitItem())

                repository.setSortOrder(SortOrder.NAME_DESC)

                assertEquals(SortOrder.NAME_DESC, awaitItem())
            }
        }
}
