package ru.marwinka.androidarchsample.feature.characters.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

internal interface CharacterApi {
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
    ): CharacterListResponseDto
}
