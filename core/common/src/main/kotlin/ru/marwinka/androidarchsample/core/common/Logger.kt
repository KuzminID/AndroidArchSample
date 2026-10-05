package ru.marwinka.androidarchsample.core.common

/** Logging facade for features; the implementation is bound in the app. */
interface Logger {
    fun debug(message: String)

    fun warn(
        message: String,
        throwable: Throwable? = null,
    )

    fun error(
        message: String,
        throwable: Throwable? = null,
    )
}
