package com.tarripoha.android.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.BaseViewModel
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.util.TPUtils
import java.lang.Exception
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
  private val postComment: MutableLiveData<Comment> = MutableLiveData()

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

  fun setPostComment(comment: Comment?) {
    postComment.value = comment
  }

  fun getPostComment() = postComment

  // Helper Functions

  private fun isInternetConnected(): Boolean {
    if (!TPUtils.isNetworkAvailable(getContext())) {
      setUserMessage(getString(R.string.error_no_internet))
      return false
    }
    return true
  }

  fun addNewWord(word: Word) {
    if (!isInternetConnected()) {
      return
    }
    isRefreshing.value = true
    repository.addNewWord(
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
    if (!isInternetConnected()) {
      return
    }
    isRefreshing.value = true
    repository.fetchAllWords(
        success = { snapshot ->
          handleFetchAllResponse(snapshot = snapshot)
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

  private fun handleFetchAllResponse(
    snapshot: DataSnapshot
  ) {
    val wordList: MutableList<Word> = mutableListOf()
    snapshot.children.forEach {

      try {
        if (it.getValue(Word::class.java) != null) {
          val word: Word = it.getValue(Word::class.java)!!
          val isDirty = word.dirty
          if (isDirty == null || !isDirty) {
            wordList.add(word)
          } else {
            Log.i(TAG, "handleFetchAllResponse: ${word.name} found dirty")
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "handleFetchAllResponse: ${e.localizedMessage}")
      }
    }
    words.value = wordList
    wordCount.value = wordList.size
    isRefreshing.value = false
  }

  fun search(query: String) {
    if (!isInternetConnected()) {
      return
    }
    repository.searchWord(
        word = query,
        success = { snapshot ->
          handleSearchResponse(query = query, snapshot = snapshot)
        },
        failure = {
          setUserMessage(getString(R.string.error_unable_to_fetch))
        },
        connectionStatus = {

        })
  }

  private fun handleSearchResponse(
    query: String,
    snapshot: DataSnapshot
  ) {
    Log.d(TAG, "handleSearchResponse: query $query size ${query.length}")
    val wordList: MutableList<Word> = mutableListOf()
    var wordFound = false
    snapshot.children.forEach {
      try {
        if (it.getValue(Word::class.java) != null) {
          val w: Word = it.getValue(Word::class.java)!!
          val isDirty = w.dirty
          if (isDirty == null || !isDirty) {
            wordList.add(w)
          } else {
            Log.i(TAG, "handleSearchResponse: ${w.name} found dirty")
          }
          if (w.name == query) {
            wordFound = true
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "handleSearchResponse: ${e.localizedMessage}")
      }
    }
    if (!wordFound) wordList.add(Word.getNewWord(name = query))
    setSearchWords(wordList)
  }

  fun postComment(comment: Comment) {
    if (!isInternetConnected()) {
      return
    }
    repository.postComment(
        comment = comment,
        success = {
          setUserMessage(getString(R.string.succ_data_added))
        },
        failure = {
          setUserMessage(getString(R.string.error_unable_to_process))
        },
        connectionStatus = {

        }
    )
  }

  private fun handlePostCommentResponse() {

  }

  // endregion

  companion object {
    private const val TAG = "MainViewModel"
  }
}