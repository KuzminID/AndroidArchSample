package ru.marwinka.androidarchsample.feature.characters.data.remote

/** Serves [pages] in order; [failure] is thrown instead when set. */
internal class FakeCharacterApi(
    private val pages: List<List<CharacterDto>> = emptyList(),
) : CharacterApi {
    var failure: Throwable? = null
    val requestedPages = mutableListOf<Int>()

    override suspend fun getCharacters(page: Int): CharacterListResponseDto {
        requestedPages += page
        failure?.let { throw it }
        val next = if (page < pages.size) "https://rickandmortyapi.com/api/character?page=${page + 1}" else null
        return CharacterListResponseDto(CharacterListInfoDto(next), pages.getOrElse(page - 1) { emptyList() })
    }
}
