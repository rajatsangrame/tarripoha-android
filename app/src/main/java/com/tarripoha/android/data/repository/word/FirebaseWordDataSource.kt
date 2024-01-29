package com.tarripoha.android.data.repository.word

import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.WordDataSource
import com.tarripoha.android.util.ktx.FirebaseUtil.fireStoreFind
import timber.log.Timber


class FirebaseWordDataSource(private val wordRef: CollectionReference) : WordDataSource {

    override suspend fun add(word: Word) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: Long): Word? {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(): List<Word> {
        val snapshot = wordRef.fireStoreFind()
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
