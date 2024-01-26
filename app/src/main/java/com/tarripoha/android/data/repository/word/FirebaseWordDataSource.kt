package com.tarripoha.android.data.repository.word

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.WordDataSource
import com.tarripoha.android.utils.ktx.FirebaseUtil.findAllForSingleEvent
import timber.log.Timber


class FirebaseWordDataSource(private val wordRef: DatabaseReference) : WordDataSource {

    override suspend fun add(word: Word) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: Long): Word? {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(): List<Word> {
        val snapshot = wordRef.findAllForSingleEvent()
        return parseWordList(snapshot)
    }

    override suspend fun remove(word: Word) {
        TODO("Not yet implemented")
    }

    private fun parseWordList(snapshot: DataSnapshot): List<Word> {
        val result = mutableListOf<Word>()
        snapshot.children.forEach {
            try {
                val word: Word? = it.getValue(Word::class.java)
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
