package ru.marwinka.androidarchsample.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.network.CharacterApi
import ru.marwinka.androidarchsample.core.network.model.CharacterDto
import ru.marwinka.androidarchsample.core.network.toAppError
import ru.marwinka.androidarchsample.core.preferences.UserPreferences
import ru.marwinka.androidarchsample.data.local.CharacterDao
import ru.marwinka.androidarchsample.data.mapper.toDomain
import ru.marwinka.androidarchsample.data.mapper.toEntity
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository
import javax.inject.Inject

class CharacterRepositoryImpl
    @Inject
    constructor(
        private val api: CharacterApi,
        private val dao: CharacterDao,
        private val preferences: UserPreferences,
        private val dispatchers: DispatcherProvider,
    ) : CharacterRepository {
        override fun observeCharacters(sortOrder: SortOrder): Flow<List<Character>> {
            val entities =
                when (sortOrder) {
                    SortOrder.NAME_ASC -> dao.observeAllSortedByNameAsc()
                    SortOrder.NAME_DESC -> dao.observeAllSortedByNameDesc()
                }
            return combine(entities, preferences.observeFavoriteIds()) { characters, favoriteIds ->
                characters.map { it.toDomain(isFavorite = favoriteIds.contains(it.id.toString())) }
            }.flowOn(dispatchers.io)
        }

        override fun observeCharacter(id: Int): Flow<Character?> =
            combine(dao.observeById(id), preferences.observeFavoriteIds()) { entity, favoriteIds ->
                entity?.toDomain(isFavorite = favoriteIds.contains(id.toString()))
            }.flowOn(dispatchers.io)

        override suspend fun refresh(): AppResult<Unit> =
            withContext(dispatchers.io) {
                try {
                    val allResults = mutableListOf<CharacterDto>()
                    var page = 1
                    while (true) {
                        val response = api.getCharacters(page)
                        allResults += response.results
                        if (response.info.next == null) break
                        page++
                    }
                    dao.replaceAll(allResults.map { it.toEntity() })
                    AppResult.Success(Unit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppResult.Failure(e.toAppError())
                }
            }

        override fun observeSortOrder(): Flow<SortOrder> =
            preferences
                .observeSortOrder()
                .map { raw ->
                    runCatching { SortOrder.valueOf(raw) }.getOrDefault(SortOrder.NAME_ASC)
                }.flowOn(dispatchers.io)

        override suspend fun setSortOrder(order: SortOrder) {
            withContext(dispatchers.io) {
                preferences.setSortOrder(order.name)
            }
        }

        override suspend fun toggleFavorite(id: Int) {
            withContext(dispatchers.io) {
                preferences.toggleFavorite(id.toString())
            }
        }
    }
