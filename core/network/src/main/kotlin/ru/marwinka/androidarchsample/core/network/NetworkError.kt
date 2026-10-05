package ru.marwinka.androidarchsample.core.network

import retrofit2.HttpException
import ru.marwinka.androidarchsample.core.common.AppError
import java.io.IOException

private const val HTTP_NOT_FOUND = 404

/** Maps a network exception to [AppError]. Pass it to `appResultOf` as `mapError`. */
fun Throwable.toNetworkError(): AppError =
    when (this) {
        is HttpException -> if (code() == HTTP_NOT_FOUND) AppError.NotFound(this) else AppError.Unknown(this)
        is IOException -> AppError.Network(this)
        else -> AppError.Unknown(this)
    }
