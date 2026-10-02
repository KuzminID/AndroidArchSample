package ru.marwinka.androidarchsample.feature.characters.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.marwinka.androidarchsample.core.testing.MainDispatcherRule
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.testing.FakeCharacterRepository
import ru.marwinka.androidarchsample.domain.usecase.GetCharacterDetailUseCase
import ru.marwinka.androidarchsample.domain.usecase.ToggleFavoriteUseCase
import ru.marwinka.androidarchsample.feature.characters.navigation.CharacterDetailDestination

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val rick = Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = false)

    private fun viewModel(
        repository: FakeCharacterRepository,
        savedStateHandle: SavedStateHandle =
            SavedStateHandle(mapOf(CharacterDetailDestination::characterId.name to rick.id)),
    ) = CharacterDetailViewModel(
        savedStateHandle = savedStateHandle,
        getCharacterDetail = GetCharacterDetailUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
    )

    @Test
    fun `loads the character matching the saved state id`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))

            viewModel(repository).uiState.test {
                val state = awaitItem()
                assertEquals(rick, state.character)
                assertFalse(state.isLoading)
            }
        }

    @Test
    fun `missing character id argument fails fast`() {
        val repository = FakeCharacterRepository(listOf(rick))

        assertThrows(IllegalStateException::class.java) {
            viewModel(repository, savedStateHandle = SavedStateHandle())
        }
    }

    @Test
    fun `onFavoriteClick toggles the character's favorite flag`() =
        runTest {
            val repository = FakeCharacterRepository(listOf(rick))
            val viewModel = viewModel(repository)

            viewModel.uiState.test {
                assertFalse(awaitItem().character!!.isFavorite)

                viewModel.onFavoriteClick()

                assertTrue(awaitItem().character!!.isFavorite)
            }
        }
}
