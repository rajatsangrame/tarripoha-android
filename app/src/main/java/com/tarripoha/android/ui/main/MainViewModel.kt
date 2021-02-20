package com.tarripoha.android.ui.main

import androidx.lifecycle.*
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class MainViewModel @Inject constructor(var repository: Repository) : ViewModel() {

  private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()

  private val words: MutableLiveData<List<Word>> = MutableLiveData()

  fun isRefreshing() = isRefreshing

  fun getAllWords() = words

  fun addWord(word: Word) {
    isRefreshing.value = true
    repository.addWord(word,
        {
          isRefreshing.value = false
        },
        {
          isRefreshing.value = false
        }
    )
  }

  fun fetchAllWord() {
    isRefreshing.value = true
    repository.fetchAllWords(
        { snapshot ->
          val wordList: MutableList<Word> = mutableListOf()
          snapshot.children.forEach {
            if (it.getValue(Word::class.java) != null) {
              val word: Word = it.getValue(Word::class.java)!!
              wordList.add(word)
            }
          }
          words.value = wordList
          isRefreshing.value = false
        },
        {
          isRefreshing.value = false
        }
    )
  }

  companion object {
    private const val TAG = "MainViewModel"
  }
}