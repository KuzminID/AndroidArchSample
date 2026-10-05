package ru.marwinka.androidarchsample.core.testing

import kotlinx.coroutines.test.TestDispatcher
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.common.Logger

/** Routes every dispatcher to [dispatcher]; build it on the `runTest` scheduler. */
class TestDispatcherProvider(
    dispatcher: TestDispatcher,
) : DispatcherProvider {
    override val io = dispatcher
    override val default = dispatcher
    override val main = dispatcher
}

/** Records logged errors for assertions. */
class RecordingLogger : Logger {
    val errors = mutableListOf<Pair<String, Throwable?>>()

    override fun debug(message: String) = Unit

    override fun warn(
        message: String,
        throwable: Throwable?,
    ) = Unit

    override fun error(
        message: String,
        throwable: Throwable?,
    ) {
        errors += message to throwable
    }
}
