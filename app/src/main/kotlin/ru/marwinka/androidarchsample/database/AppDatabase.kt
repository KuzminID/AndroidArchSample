package ru.marwinka.androidarchsample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.marwinka.androidarchsample.data.local.CharacterDao
import ru.marwinka.androidarchsample.data.local.CharacterEntity

/** Room database of the app. Entities and DAOs live in feature `data` modules. */
@Database(entities = [CharacterEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}
