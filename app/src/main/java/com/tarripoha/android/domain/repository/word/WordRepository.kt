package com.tarripoha.android.domain.repository.word

import com.tarripoha.android.domain.entity.Word

class WordRepository(private val dataSource: WordDataSource) {
    suspend fun addWord(word: Word) = dataSource.add(word)
    suspend fun getWordDetail(id: Long) = dataSource.getWordDetail(id)
    suspend fun getAll() = dataSource.getAllWords()

    suspend fun getFilteredWords(params: FilterParams) = dataSource.getFilteredWords(params)
    suspend fun removeWord(word: Word) = dataSource.remove(word)

    data class FilterParams(
        val field: String,
        val value: String,
        var sortField: String? = null,
        var asc: Boolean? = null,
        var cursor: String? = null,
        val limit: Long = 20
    )
}
