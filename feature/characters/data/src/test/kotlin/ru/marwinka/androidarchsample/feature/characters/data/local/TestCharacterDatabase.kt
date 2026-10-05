package ru.marwinka.androidarchsample.feature.characters.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider

/** In-memory database with this feature's tables only; the app's AppDatabase is not visible here. */
@Database(entities = [CharacterEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
abstract class TestCharacterDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        fun create(): TestCharacterDatabase =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TestCharacterDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
