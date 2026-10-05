package ru.marwinka.androidarchsample.feature.characters.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.common.Logger
import ru.marwinka.androidarchsample.core.common.appResultOf
import ru.marwinka.androidarchsample.core.common.logUnexpected
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository
import java.io.IOException
import javax.inject.Inject

/** Keys are prefixed with the feature name: the DataStore from core:settings is shared. */
internal val KEY_SORT_ORDER = stringPreferencesKey("characters_sort_order")

internal class CharacterSettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val dispatchers: DispatcherProvider,
        private val logger: Logger,
    ) : CharacterSettingsRepository {
        override fun observeSortOrder(): Flow<SortOrder> =
            dataStore.data
                .catch { error ->
                    if (error !is IOException) throw error
                    logger.warn("Reading character settings failed", error)
                    emit(emptyPreferences())
                }.map { preferences ->
                    preferences[KEY_SORT_ORDER]?.let { stored -> SortOrder.entries.firstOrNull { it.name == stored } }
                        ?: SortOrder.NAME_ASC
                }.distinctUntilChanged()

        override suspend fun setSortOrder(order: SortOrder): AppResult<Unit> =
            appResultOf(dispatchers.io) {
                dataStore.edit { it[KEY_SORT_ORDER] = order.name }
                Unit
            }.logUnexpected(logger, "CharacterSettingsRepository.setSortOrder")
    }
