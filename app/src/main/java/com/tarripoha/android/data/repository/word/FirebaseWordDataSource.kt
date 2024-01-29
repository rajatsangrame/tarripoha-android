package com.tarripoha.android.data.repository.word

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordDataSource
import com.tarripoha.android.domain.repository.word.WordRepository.FilterParams
import com.tarripoha.android.util.ktx.FirebaseUtil.cloudStorageFind
import timber.log.Timber


class FirebaseWordDataSource(private val wordRef: CollectionReference) : WordDataSource {

    override suspend fun add(word: Word) {
        TODO("Not yet implemented")
    }

    override suspend fun getWordDetail(id: Long): Word? {
        TODO("Not yet implemented")
    }

    override suspend fun getFilteredWords(params: Any): List<Word> {
        val (field, value, sortField, asc, cursor, limit) = (params as FilterParams)
        val direction = if (asc == true) Query.Direction.ASCENDING else Query.Direction.DESCENDING
        val query = if (false) {
            wordRef.whereEqualTo(field, value).orderBy(sortField!!, direction).limit(limit)
        } else {
            wordRef.whereEqualTo(field, value).limit(limit)
        }
        val snapshot = query.cloudStorageFind()
        return parseWordList(snapshot)
    }

    override suspend fun getAllWords(): List<Word> {
        val snapshot = wordRef.cloudStorageFind()
        return parseWordList(snapshot)
    }

    override suspend fun remove(word: Word) {
        TODO("Not yet implemented")
    }

    private fun parseWordList(snapshot: QuerySnapshot): List<Word> {
        val result = mutableListOf<Word>()
        snapshot.documents.forEach {
            try {
                val word: Word? = it.toObject(Word::class.java)
                if (word?.isDirty() == false && word.isApproved()) {
                    result.add(word)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return result
    }
}
