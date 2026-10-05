package ru.marwinka.androidarchsample.feature.characters.presentation.list

import ru.marwinka.androidarchsample.core.common.AppError

internal sealed interface RefreshState {
    data object Running : RefreshState

    data object Succeeded : RefreshState

    /** [attempt] tells failures apart, so dismissing one does not hide the next. */
    data class Failed(
        val error: AppError,
        val attempt: Int,
    ) : RefreshState
}
