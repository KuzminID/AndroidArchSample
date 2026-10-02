package ru.marwinka.androidarchsample.feature.characters.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.usecase.GetCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.GetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.RefreshCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.SetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.ToggleFavoriteUseCase
import javax.inject.Inject

@HiltViewModel
class CharactersListViewModel
    @Inject
    constructor(
        private val getCharacters: GetCharactersUseCase,
        private val getSortOrder: GetSortOrderUseCase,
        private val refreshCharacters: RefreshCharactersUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
        private val setSortOrder: SetSortOrderUseCase,
    ) : ViewModel() {
        private val isLoading = MutableStateFlow(false)
        private val error = MutableStateFlow<CharactersListError?>(null)

        val uiState: StateFlow<CharactersListUiState> =
            combine(
                getCharacters(),
                getSortOrder(),
                isLoading,
                error,
            ) { characters, sortOrder, loading, error ->
                CharactersListUiState(
                    characters = characters,
                    sortOrder = sortOrder,
                    isLoading = loading,
                    error = error,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CharactersListUiState(isLoading = true),
            )

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                isLoading.value = true
                error.value = null
                when (val result = refreshCharacters()) {
                    is AppResult.Failure -> error.value = result.error.toListError()
                    is AppResult.Success -> Unit
                }
                isLoading.value = false
            }
        }

        fun onErrorShown() {
            error.value = null
        }

        fun onFavoriteClick(characterId: Int) {
            viewModelScope.launch { toggleFavorite(characterId) }
        }

        fun onSortOrderSelected(sortOrder: SortOrder) {
            viewModelScope.launch { setSortOrder(sortOrder) }
        }

        private fun AppError.toListError(): CharactersListError =
            when (this) {
                is AppError.Network -> CharactersListError.NETWORK
                is AppError.NotFound, is AppError.Unknown -> CharactersListError.REFRESH_FAILED
            }
    }
