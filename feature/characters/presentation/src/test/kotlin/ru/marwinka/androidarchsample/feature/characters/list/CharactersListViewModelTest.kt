package ru.marwinka.androidarchsample.feature.characters.list

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.testing.MainDispatcherRule
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository
import ru.marwinka.androidarchsample.domain.usecase.GetCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.GetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.RefreshCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.SetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.ToggleFavoriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)
    private val morty = Character(2, "Morty Smith", "Human", "morty.png", isFavorite = false)

    private fun viewModel(repository: FakeCharacterRepository) =
        CharactersListViewModel(
            getCharacters = GetCharactersUseCase(repository),
            getSortOrder = GetSortOrderUseCase(repository),
            refreshCharacters = RefreshCharactersUseCase(repository),
            toggleFavorite = ToggleFavoriteUseCase(repository),
            setSortOrder = SetSortOrderUseCase(repository),
        )

    @Test
    fun `loads cached characters and refreshes on init`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(morty, rick))

            viewModel(repository).uiState.test {
                val state = awaitItem()
                assertEquals(listOf(morty, rick), state.characters)
                assertFalse(state.isLoading)
                assertNull(state.error)
            }
            assertEquals(1, repository.refreshCallCount)
        }

    @Test
    fun `a failed refresh surfaces an error kind`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            repository.refreshResult = AppResult.Failure(AppError.Unknown(IllegalStateException("boom")))

            viewModel(repository).uiState.test {
                val state = awaitItem()
                assertEquals(CharactersListError.REFRESH_FAILED, state.error)
            }
        }

    @Test
    fun `a network failure is reported as a NETWORK error`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            repository.refreshResult = AppResult.Failure(AppError.Network())

            viewModel(repository).uiState.test {
                assertEquals(CharactersListError.NETWORK, awaitItem().error)
            }
        }

    @Test
    fun `onErrorShown clears the error`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            repository.refreshResult = AppResult.Failure(AppError.Unknown(IllegalStateException("boom")))
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertEquals(CharactersListError.REFRESH_FAILED, awaitItem().error)

                viewModel.onErrorShown()

                assertNull(awaitItem().error)
            }
        }

    @Test
    fun `onFavoriteClick toggles the character's favorite flag`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertFalse(awaitItem().characters.first().isFavorite)

                viewModel.onFavoriteClick(rick.id)

                assertTrue(awaitItem().characters.first().isFavorite)
            }
        }

    @Test
    fun `onSortOrderSelected re-sorts the character list`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(morty, rick))
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertEquals(listOf(morty, rick), awaitItem().characters)

                viewModel.onSortOrderSelected(SortOrder.NAME_DESC)

                // State may settle over several emissions; check the final one.
                assertEquals(listOf(rick, morty), viewModel.uiState.value.characters)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
