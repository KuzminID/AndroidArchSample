package ru.marwinka.androidarchsample.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun observeAllSortedByNameAsc(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters ORDER BY name DESC")
    fun observeAllSortedByNameDesc(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun observeById(id: Int): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun deleteAll()

    /** Replaces all cached characters in one transaction. */
    @Transaction
    suspend fun replaceAll(characters: List<CharacterEntity>) {
        deleteAll()
        insertAll(characters)
    }
}
