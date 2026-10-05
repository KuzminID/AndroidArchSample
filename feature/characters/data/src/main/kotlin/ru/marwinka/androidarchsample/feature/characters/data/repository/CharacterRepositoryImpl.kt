package ru.marwinka.androidarchsample.feature.characters.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.core.common.DispatcherProvider
import ru.marwinka.androidarchsample.core.common.Logger
import ru.marwinka.androidarchsample.core.common.appResultOf
import ru.marwinka.androidarchsample.core.common.logUnexpected
import ru.marwinka.androidarchsample.core.network.toNetworkError
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterDao
import ru.marwinka.androidarchsample.feature.characters.data.mapper.toDomain
import ru.marwinka.androidarchsample.feature.characters.data.mapper.toEntity
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterApi
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterDto
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import javax.inject.Inject

/** Offline-first: screens read Room, [refresh] fills it from the API. HTTP 429 is retried in core:network. */
internal class CharacterRepositoryImpl
    @Inject
    constructor(
        private val api: CharacterApi,
        private val dao: CharacterDao,
        private val dispatchers: DispatcherProvider,
        private val logger: Logger,
    ) : CharacterRepository {
        override fun observeCharacters(sortOrder: SortOrder): Flow<List<Character>> {
            val rows =
                when (sortOrder) {
                    SortOrder.NAME_ASC -> dao.observeAllSortedByNameAsc()
                    SortOrder.NAME_DESC -> dao.observeAllSortedByNameDesc()
                }
            return rows.map { list -> list.map { it.toDomain() } }.flowOn(dispatchers.default)
        }

        override fun observeCharacter(id: Int): Flow<Character?> =
            dao
                .observeById(id)
                .map {
                    it?.toDomain()
                }.flowOn(dispatchers.default)

        override suspend fun refresh(): AppResult<Unit> =
            appResultOf(dispatchers.io, mapError = Throwable::toNetworkError) {
                dao.replaceAll(fetchAllPages().map { it.toEntity() })
            }.logUnexpected(logger, "CharacterRepository.refresh")

        override suspend fun toggleFavorite(id: Int): AppResult<Unit> =
            appResultOf(dispatchers.io) { dao.toggleFavorite(id) }
                .logUnexpected(logger, "CharacterRepository.toggleFavorite")

        private suspend fun fetchAllPages(): List<CharacterDto> =
            buildList {
                var page = 1
                do {
                    val response = api.getCharacters(page++)
                    addAll(response.results)
                } while (response.info.next != null)
            }
    }
