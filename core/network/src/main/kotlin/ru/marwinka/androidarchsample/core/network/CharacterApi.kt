package ru.marwinka.androidarchsample.core.network

import kotlinx.serialization.InternalSerializationApi
import retrofit2.http.GET
import retrofit2.http.Query
import ru.marwinka.androidarchsample.core.network.model.CharacterListResponseDto

interface CharacterApi {
    @OptIn(InternalSerializationApi::class)
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int = 1,
    ): CharacterListResponseDto
}
