package com.tarripoha.android.data.datasource.word

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordDataSource
import com.tarripoha.android.domain.repository.word.WordRepository.FilterParams
import com.tarripoha.android.util.helper.FirebaseHelper.CloudStore.buildWhereEqualTo
import com.tarripoha.android.util.helper.FirebaseHelper.CloudStore.findItems


class FirebaseWordDataSource(private val wordRef: CollectionReference) : WordDataSource {

    override suspend fun add(word: Word) {
        TODO("Not yet implemented")
    }

    override suspend fun getWordDetail(id: Long): Word? {
        TODO("Not yet implemented")
    }

    override suspend fun getFilteredWords(params: Any): List<Word> {

        val (data, sortField, asc, cursor, limit) = (params as FilterParams)
        val direction = if (asc == true) Query.Direction.ASCENDING else Query.Direction.DESCENDING
        var query = wordRef.buildWhereEqualTo(data)
        if (sortField != null) {
            query = query.orderBy(sortField, direction)
        }
        query.limit(limit)
        return query.findItems()
    }

    override suspend fun getAllWords(): List<Word> {
        val data = mutableMapOf<String, Any>().also {
            it["dirty"] = false
            it["approved"] = true
        }
        val query = wordRef.buildWhereEqualTo(data)
        return query.findItems()
    }

    override suspend fun remove(word: Word) {
        TODO("Not yet implemented")
    }
}
