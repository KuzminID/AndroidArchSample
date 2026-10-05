package ru.marwinka.androidarchsample.feature.characters.presentation.list

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
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
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.testing.FakeCharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.testing.FakeCharacterSettingsRepository
import ru.marwinka.androidarchsample.feature.characters.domain.usecase.ObserveCharactersUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)
    private val morty = Character(2, "Morty Smith", "Human", "morty.png", isFavorite = false)

    private val settings = FakeCharacterSettingsRepository()

    private fun viewModel(repository: FakeCharacterRepository) =
        CharactersListViewModel(ObserveCharactersUseCase(repository, settings), repository, settings)

    private fun failure(error: AppError) = AppResult.Failure(error)

    @Test
    fun `an empty cache before the first refresh completes keeps the initial loading`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository =
                FakeCharacterRepository().apply {
                    refreshGate = gate
                    remoteCharacters = listOf(rick)
                }

            viewModel(repository).uiState.test {
                val loading = awaitItem()
                assertTrue(loading.isInitialLoading)
                assertFalse(loading.isRefreshing)

                gate.complete(Unit)

                val loaded = expectMostRecentItem()
                assertFalse(loaded.isInitialLoading)
                assertEquals(listOf(rick), loaded.characters)
            }
        }

    @Test
    fun `cached data is shown at once and the refresh runs over it`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeCharacterRepository(listOf(rick)).apply { refreshGate = gate }

            viewModel(repository).uiState.test {
                val refreshing = awaitItem()
                assertFalse(refreshing.isInitialLoading)
                assertTrue(refreshing.isRefreshing)
                assertEquals(listOf(rick), refreshing.characters)

                gate.complete(Unit)

                assertFalse(awaitItem().isRefreshing)
            }
            assertEquals(1, repository.refreshCallCount)
        }

    @Test
    fun `a failed refresh with an empty cache is a full screen error`() =
        runTest {
            val repository = FakeCharacterRepository().apply { refreshResult = failure(AppError.Network()) }

            viewModel(repository).uiState.test {
                val state = awaitItem()
                assertFalse(state.isInitialLoading)
                assertEquals(CharactersListError.NETWORK, state.refreshError)
                assertNull(state.message)
            }
        }

    @Test
    fun `a failed refresh over cached data is a snackbar message that can be dismissed`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick)).apply { refreshResult = failure(AppError.Unknown()) }
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(listOf(rick), state.characters)
                assertEquals(CharactersListError.REFRESH_FAILED, state.message)

                viewModel.onMessageShown()

                assertNull(awaitItem().message)
            }
        }

    @Test
    fun `retry after a failure refreshes again and clears the error`() =
        runTest {
            val repository = FakeCharacterRepository().apply { refreshResult = failure(AppError.Network()) }
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertEquals(CharactersListError.NETWORK, awaitItem().refreshError)

                repository.refreshResult = AppResult.Success(Unit)
                repository.remoteCharacters = listOf(rick)
                viewModel.onRefresh()

                val state = expectMostRecentItem()
                assertNull(state.refreshError)
                assertEquals(listOf(rick), state.characters)
            }
            assertEquals(2, repository.refreshCallCount)
        }

    @Test
    fun `onRefresh while a refresh is running does not start another one`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeCharacterRepository(listOf(rick)).apply { refreshGate = gate }
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertTrue(awaitItem().isRefreshing)

                viewModel.onRefresh()
                viewModel.onRefresh()
                gate.complete(Unit)

                assertFalse(expectMostRecentItem().isRefreshing)
            }
            assertEquals(1, repository.refreshCallCount)
        }

    @Test
    fun `onFavoriteClick toggles the favorite flag`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertFalse(awaitItem().characters.single().isFavorite)

                viewModel.onFavoriteClick(rick.id)

                assertTrue(awaitItem().characters.single().isFavorite)
            }
        }

    @Test
    fun `a failed favorite toggle is reported as a message`() =
        runTest {
            val repository =
                FakeCharacterRepository(listOf(rick)).apply {
                    toggleFavoriteResult =
                        failure(AppError.Unknown())
                }
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                awaitItem()

                viewModel.onFavoriteClick(rick.id)

                assertEquals(CharactersListError.ACTION_FAILED, awaitItem().message)
            }
        }

    @Test
    fun `onSortOrderSelected re-sorts the list and updates the sort order`() =
        runTest {
            val viewModel = viewModel(FakeCharacterRepository(listOf(rick, morty)))

            viewModel.uiState.test {
                assertEquals(listOf(morty, rick), awaitItem().characters)

                viewModel.onSortOrderSelected(SortOrder.NAME_DESC)

                val state = expectMostRecentItem()
                assertEquals(SortOrder.NAME_DESC, state.sortOrder)
                assertEquals(listOf(rick, morty), state.characters)
            }
        }
}
