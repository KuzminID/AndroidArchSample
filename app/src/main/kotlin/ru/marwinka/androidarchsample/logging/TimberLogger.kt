package ru.marwinka.androidarchsample.logging

import ru.marwinka.androidarchsample.core.common.Logger
import timber.log.Timber
import javax.inject.Inject

class TimberLogger
    @Inject
    constructor() : Logger {
        override fun debug(message: String) = Timber.d(message)

        override fun warn(
            message: String,
            throwable: Throwable?,
        ) = Timber.w(throwable, message)

        override fun error(
            message: String,
            throwable: Throwable?,
        ) = Timber.e(throwable, message)
    }
