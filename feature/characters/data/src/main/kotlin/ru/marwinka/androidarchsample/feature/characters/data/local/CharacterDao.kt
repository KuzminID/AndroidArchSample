package ru.marwinka.androidarchsample.feature.characters.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

private const val WITH_FAVORITE =
    "SELECT characters.*, EXISTS(SELECT 1 FROM favorites WHERE favorites.characterId = characters.id) AS isFavorite " +
        "FROM characters"

@Dao
interface CharacterDao {
    @Query("$WITH_FAVORITE ORDER BY name ASC")
    fun observeAllSortedByNameAsc(): Flow<List<CharacterWithFavorite>>

    @Query("$WITH_FAVORITE ORDER BY name DESC")
    fun observeAllSortedByNameDesc(): Flow<List<CharacterWithFavorite>>

    @Query("$WITH_FAVORITE WHERE id = :id")
    fun observeById(id: Int): Flow<CharacterWithFavorite?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun deleteAll()

    /** Replaces all cached characters in one transaction. Favorites are kept. */
    @Transaction
    suspend fun replaceAll(characters: List<CharacterEntity>) {
        deleteAll()
        insertAll(characters)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE characterId = :id")
    suspend fun deleteFavorite(id: Int): Int

    /** Adds or removes a favorite atomically. */
    @Transaction
    suspend fun toggleFavorite(id: Int) {
        if (deleteFavorite(id) == 0) insertFavorite(FavoriteEntity(id))
    }
}
