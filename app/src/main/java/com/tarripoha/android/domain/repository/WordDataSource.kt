package com.tarripoha.android.domain.repository
import com.tarripoha.android.domain.entity.Word

interface WordDataSource {
    suspend fun add(word: Word)
    suspend fun get(id: Long): Word?
    suspend fun getAll(): List<Word>
    suspend fun remove(word: Word)
}
