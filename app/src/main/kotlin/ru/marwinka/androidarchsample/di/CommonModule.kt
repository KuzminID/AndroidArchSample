package ru.marwinka.androidarchsample.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.marwinka.androidarchsample.core.common.DefaultDispatcherProvider
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.common.Logger
import ru.marwinka.androidarchsample.logging.TimberLogger
import javax.inject.Singleton

/** Bindings for core:common, which is a plain Kotlin module without Hilt. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindLogger(impl: TimberLogger): Logger
}
