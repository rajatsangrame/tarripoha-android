package com.tarripoha.android.domain.repository.word

import com.tarripoha.android.domain.entity.Word

interface WordDataSource {
    suspend fun add(word: Word)
    suspend fun getWordDetail(id: Long): Word?
    suspend fun getFilteredWords(params: Any): List<Word>
    suspend fun getAllWords(): List<Word>
    suspend fun remove(word: Word)
}
