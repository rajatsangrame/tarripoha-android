package com.tarripoha.android.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.BaseViewModel
import com.tarripoha.android.R
import com.tarripoha.android.util.TPUtils
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class MainViewModel @Inject constructor(
  var repository: Repository,
  app: Application
) : BaseViewModel(app) {

  private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
  private val words: MutableLiveData<List<Word>> = MutableLiveData()
  private val searchWords: MutableLiveData<List<Word>> = MutableLiveData()
  private val query: MutableLiveData<String> = MutableLiveData()
  private val wordCount: MutableLiveData<Int> = MutableLiveData()

  // region WordDetailFragment

  private val wordDetail: MutableLiveData<Word> = MutableLiveData()

  // endregion

  fun getWordCount() = wordCount

  fun isRefreshing() = isRefreshing

  fun getAllWords() = words

  fun getQuery() = query

  fun setQuery(query: String?) {
    this.query.value = query
  }

  fun getSearchWords() = searchWords

  fun setSearchWords(words: List<Word>?) {
    searchWords.value = words
  }

  fun setWordDetail(word: Word?) {
    wordDetail.value = word
  }

  fun getWordDetail() = wordDetail

  // Helper Functions

  fun addWord(word: Word) {
    if (!TPUtils.isNetworkAvailable(getContext())) {
      setUserMessage(getString(R.string.error_no_internet))
      return
    }
    isRefreshing.value = true
    repository.addWord(
      word = word,
      success = {
        isRefreshing.value = false
        setUserMessage(getString(R.string.succ_data_added))
      },
      failure = {
        isRefreshing.value = false
        setUserMessage(getString(R.string.error_unable_to_process))
      },
      connectionStatus = {
        if (!it) isRefreshing.value = false
      }
    )
  }

  fun fetchAllWord() {
    if (!TPUtils.isNetworkAvailable(getContext())) {
      setUserMessage(getString(R.string.error_no_internet))
      return
    }
    isRefreshing.value = true
    repository.fetchAllWords(
      success = { snapshot ->
        val wordList: MutableList<Word> = mutableListOf()
        snapshot.children.forEach {
          if (it.getValue(Word::class.java) != null) {
            val word: Word = it.getValue(Word::class.java)!!
            wordList.add(word)
          }
        }
        words.value = wordList
        wordCount.value = wordList.size
        isRefreshing.value = false
      },
      failure = {
        isRefreshing.value = false
        setUserMessage(getString(R.string.error_unable_to_fetch))
      },
      connectionStatus = {
        if (!it) isRefreshing.value = false
      }
    )
  }

  fun search(word: String) {
    if (!TPUtils.isNetworkAvailable(getContext())) {
      setUserMessage(getString(R.string.error_no_internet))
      return
    }
    repository.searchWord(
      word = word,
      success = { snapshot ->
        val wordList: MutableList<Word> = mutableListOf()
        snapshot.children.forEach {
          if (it.getValue(Word::class.java) != null) {
            val w: Word = it.getValue(Word::class.java)!!
            wordList.add(w)
          }
        }
        setSearchWords(wordList)
      }, failure = {}, connectionStatus = {})
  }

  // endregion

  companion object {
    private const val TAG = "MainViewModel"
  }
}