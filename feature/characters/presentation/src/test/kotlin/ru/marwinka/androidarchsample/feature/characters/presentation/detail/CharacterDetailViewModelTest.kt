package ru.marwinka.androidarchsample.feature.characters.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.testing.MainDispatcherRule
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.testing.FakeCharacterRepository
import ru.marwinka.androidarchsample.feature.characters.presentation.navigation.CharacterDetailDestination

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)

    private fun viewModel(
        repository: FakeCharacterRepository,
        savedStateHandle: SavedStateHandle =
            SavedStateHandle(
                mapOf(CharacterDetailDestination::characterId.name to rick.id),
            ),
    ) = CharacterDetailViewModel(savedStateHandle, repository)

    @Test
    fun `loads the character matching the saved state id`() =
        runTest {
            viewModel(FakeCharacterRepository(listOf(rick))).uiState.test {
                val state = awaitItem()
                assertEquals(rick, state.character)
                assertFalse(state.isLoading)
            }
        }

    @Test
    fun `an unknown id is not found rather than loading forever`() =
        runTest {
            viewModel(FakeCharacterRepository()).uiState.test {
                val state = awaitItem()
                assertNull(state.character)
                assertFalse(state.isLoading)
            }
        }

    @Test
    fun `missing character id argument fails fast`() {
        assertThrows(IllegalStateException::class.java) {
            viewModel(FakeCharacterRepository(listOf(rick)), savedStateHandle = SavedStateHandle())
        }
    }

    @Test
    fun `onFavoriteClick toggles the favorite flag`() =
        runTest {
            val viewModel = viewModel(FakeCharacterRepository(listOf(rick)))

            viewModel.uiState.test {
                assertFalse(awaitItem().character!!.isFavorite)

                viewModel.onFavoriteClick()

                assertTrue(awaitItem().character!!.isFavorite)
            }
        }

    @Test
    fun `a failed favorite toggle is a message that can be dismissed`() =
        runTest {
            val repository =
                FakeCharacterRepository(listOf(rick)).apply {
                    toggleFavoriteResult =
                        AppResult.Failure(AppError.Unknown())
                }
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                awaitItem()

                viewModel.onFavoriteClick()
                assertEquals(CharacterDetailError.FAVORITE_FAILED, awaitItem().message)

                viewModel.onMessageShown()
                assertNull(awaitItem().message)
            }
        }
}
