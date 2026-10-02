package ru.marwinka.androidarchsample.core.common

import kotlinx.coroutines.CoroutineDispatcher

/** Provides coroutine dispatchers. */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
