package ru.marwinka.androidarchsample.feature.characters.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import ru.marwinka.androidarchsample.core.common.AppError
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.common.onFailure
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository
import ru.marwinka.androidarchsample.feature.characters.domain.usecase.ObserveCharactersUseCase
import javax.inject.Inject

@HiltViewModel
class CharactersListViewModel
    @Inject
    constructor(
        observeCharacters: ObserveCharactersUseCase,
        private val repository: CharacterRepository,
        private val settings: CharacterSettingsRepository,
    ) : ViewModel() {
        // CONFLATED + the check in onRefresh(): at most one refresh runs at a time.
        private val refreshRequests = Channel<Unit>(Channel.CONFLATED).apply { trySend(Unit) }

        private val refreshState: StateFlow<RefreshState> =
            refreshRequests
                .receiveAsFlow()
                .withIndex()
                .transform { (attempt, _) ->
                    emit(RefreshState.Running)
                    emit(repository.refresh().toRefreshState(attempt))
                }.stateIn(viewModelScope, SharingStarted.Eagerly, RefreshState.Running)

        private val actionError = MutableStateFlow<CharactersListError?>(null)
        private val dismissedRefreshAttempt = MutableStateFlow(-1)

        val uiState: StateFlow<CharactersListUiState> =
            combine(
                observeCharacters(),
                settings.observeSortOrder(),
                refreshState,
                actionError,
                dismissedRefreshAttempt,
            ) { characters, sortOrder, refresh, actionError, dismissedAttempt ->
                // An empty list emitted before the refresh finishes does not end the initial load.
                val isInitialLoading = characters.isEmpty() && refresh is RefreshState.Running
                val failure = refresh as? RefreshState.Failed
                val refreshMessage =
                    failure
                        ?.takeIf { characters.isNotEmpty() && it.attempt != dismissedAttempt }
                        ?.error
                        ?.toListError()
                CharactersListUiState(
                    characters = characters,
                    sortOrder = sortOrder,
                    isInitialLoading = isInitialLoading,
                    isRefreshing = refresh is RefreshState.Running && !isInitialLoading,
                    refreshError = failure?.error?.toListError(),
                    message = actionError ?: refreshMessage,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CharactersListUiState(),
            )

        fun onRefresh() {
            if (refreshState.value !is RefreshState.Running) refreshRequests.trySend(Unit)
        }

        fun onMessageShown() {
            if (actionError.value != null) {
                actionError.value = null
            } else {
                (refreshState.value as? RefreshState.Failed)?.let { dismissedRefreshAttempt.value = it.attempt }
            }
        }

        fun onFavoriteClick(characterId: Int) {
            viewModelScope.launch {
                repository
                    .toggleFavorite(
                        characterId,
                    ).onFailure { actionError.value = CharactersListError.ACTION_FAILED }
            }
        }

        fun onSortOrderSelected(sortOrder: SortOrder) {
            viewModelScope.launch {
                settings.setSortOrder(sortOrder).onFailure { actionError.value = CharactersListError.ACTION_FAILED }
            }
        }

        private fun AppResult<Unit>.toRefreshState(attempt: Int): RefreshState =
            when (this) {
                is AppResult.Success -> RefreshState.Succeeded
                is AppResult.Failure -> RefreshState.Failed(error, attempt)
            }

        private fun AppError.toListError(): CharactersListError =
            when (this) {
                is AppError.Network -> CharactersListError.NETWORK
                is AppError.NotFound, is AppError.Unknown -> CharactersListError.REFRESH_FAILED
            }
    }
