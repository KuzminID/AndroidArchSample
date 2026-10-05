package ru.marwinka.androidarchsample.core.common

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/** Result of a one-shot operation. */
sealed interface AppResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

/** Typed operation error. */
sealed interface AppError {
    val cause: Throwable?

    /** Network is unavailable or the server is unreachable. */
    data class Network(
        override val cause: Throwable? = null,
    ) : AppError

    /** Resource not found. */
    data class NotFound(
        override val cause: Throwable? = null,
    ) : AppError

    /** Unexpected failure; logged where it is produced. */
    data class Unknown(
        override val cause: Throwable? = null,
    ) : AppError
}

/**
 * Runs [block] in [context] and wraps its outcome. [CancellationException] is rethrown and
 * [Error]s are not caught. [mapError] types the exception: pass e.g. `Throwable::toNetworkError`
 * from `core:network` for network calls.
 */
@Suppress("TooGenericExceptionCaught") // the boundary that turns any failure into a typed result
suspend fun <T> appResultOf(
    context: CoroutineContext = EmptyCoroutineContext,
    mapError: (Exception) -> AppError = { AppError.Unknown(it) },
    block: suspend () -> T,
): AppResult<T> =
    try {
        AppResult.Success(withContext(context) { block() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(mapError(e))
    }

/** Logs [AppError.Unknown] failures; other errors are expected and handled by the UI. */
fun <T> AppResult<T>.logUnexpected(
    logger: Logger,
    operation: String,
): AppResult<T> =
    also {
        if (it is AppResult.Failure && it.error is AppError.Unknown) {
            logger.error("$operation failed", it.error.cause)
        }
    }

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(value)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}
