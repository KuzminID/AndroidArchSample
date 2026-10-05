package ru.marwinka.androidarchsample.feature.characters.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.testing.RecordingLogger
import ru.marwinka.androidarchsample.core.testing.TestDispatcherProvider
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import java.io.File

class CharacterSettingsRepositoryImplTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.dataStore() =
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(folder.root, "test.preferences_pb") },
        )

    private fun TestScope.repository(dataStore: DataStore<Preferences>) =
        CharacterSettingsRepositoryImpl(
            dataStore,
            TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
            RecordingLogger(),
        )

    @Test
    fun `sort order defaults to NAME_ASC`() =
        runTest {
            assertEquals(SortOrder.NAME_ASC, repository(dataStore()).observeSortOrder().first())
        }

    @Test
    fun `setSortOrder is persisted and observed`() =
        runTest {
            val repository = repository(dataStore())

            assertEquals(AppResult.Success(Unit), repository.setSortOrder(SortOrder.NAME_DESC))

            assertEquals(SortOrder.NAME_DESC, repository.observeSortOrder().first())
        }

    @Test
    fun `an unknown stored value falls back to NAME_ASC`() =
        runTest {
            val dataStore = dataStore()
            dataStore.edit { it[KEY_SORT_ORDER] = "not_a_real_order" }

            assertEquals(SortOrder.NAME_ASC, repository(dataStore).observeSortOrder().first())
        }
}
