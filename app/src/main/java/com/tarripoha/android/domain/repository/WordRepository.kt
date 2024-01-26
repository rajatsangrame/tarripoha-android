package com.tarripoha.android.domain.repository

import com.tarripoha.android.domain.entity.Word

class WordRepository(private val dataSource: WordDataSource) {
    suspend fun addWord(word: Word) = dataSource.add(word)
    suspend fun getWord(id: Long) = dataSource.get(id)
    suspend fun getAll() = dataSource.getAll()
    suspend fun removeWord(word: Word) = dataSource.remove(word)
}
