package ru.marwinka.androidarchsample.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

const val DEFAULT_SORT_ORDER = "NAME_ASC"

private val KEY_FAVORITE_IDS = stringSetPreferencesKey("favorite_ids")
private val KEY_SORT_ORDER = stringPreferencesKey("sort_order")

/** Reactive wrapper over [DataStore]. */
@Singleton
class UserPreferences
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        fun observeFavoriteIds(): Flow<Set<String>> =
            dataStore.data
                .map {
                    it[KEY_FAVORITE_IDS].orEmpty()
                }.distinctUntilChanged()

        suspend fun toggleFavorite(id: String) {
            dataStore.edit { prefs ->
                val current = prefs[KEY_FAVORITE_IDS].orEmpty()
                prefs[KEY_FAVORITE_IDS] = if (id in current) current - id else current + id
            }
        }

        fun observeSortOrder(): Flow<String> =
            dataStore.data
                .map {
                    it[KEY_SORT_ORDER] ?: DEFAULT_SORT_ORDER
                }.distinctUntilChanged()

        suspend fun setSortOrder(value: String) {
            dataStore.edit { it[KEY_SORT_ORDER] = value }
        }
    }
