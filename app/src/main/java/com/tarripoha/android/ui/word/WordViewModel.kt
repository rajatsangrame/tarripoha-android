package com.tarripoha.android.ui.word

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.BaseViewModel
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.util.helper.UserHelper
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class WordViewModel @Inject constructor(
  var repository: Repository,
  app: Application
) : BaseViewModel(app) {

  private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
  private val words: MutableLiveData<List<Word>> = MutableLiveData()
  private val searchWords: MutableLiveData<List<Word>> = MutableLiveData()
  private val query: MutableLiveData<String> = MutableLiveData()
  private val wordCount: MutableLiveData<Int> = MutableLiveData()
  private var fetchMode: FetchMode = FetchMode.Popular

  // region WordDetailFragment Variable

  private val wordDetail: MutableLiveData<Word> = MutableLiveData()
  private val refreshComment: MutableLiveData<Boolean> = MutableLiveData()

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

  fun setRefreshComment(refresh: Boolean?) {
    refreshComment.value = refresh
  }

  fun getRefreshComment() = refreshComment

  fun setFetchMode(mode: FetchMode) {
    fetchMode = mode
  }

  fun getFetchMode() = fetchMode

  // Helper Functions

  fun addNewWord(word: Word) {
    if (!checkNetworkAndShowError()) {
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
    if (!checkNetworkAndShowError()) {
      return
    }
    isRefreshing.value = true
    repository.fetchAllWords(
      success = { snapshot ->
        fetchAllResponse(snapshot = snapshot)
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

  private fun fetchAllResponse(
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
            Log.i(TAG, "fetchAllResponse: ${word.name} found dirty")
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
      }
    }
    words.value = wordList
    wordCount.value = wordList.size
    isRefreshing.value = false
  }

  fun search(query: String) {
    if (!checkNetworkAndShowError()) {
      return
    }
    repository.searchWord(
      word = query,
      success = { snapshot ->
        searchResponse(query = query, snapshot = snapshot)
      },
      failure = {
        setUserMessage(getString(R.string.error_unable_to_fetch))
      },
      connectionStatus = {

      })
  }

  private fun searchResponse(
    query: String,
    snapshot: DataSnapshot
  ) {
    Log.d(TAG, "searchResponse: query $query size ${query.length}")
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
            Log.i(TAG, "searchResponse: ${w.name} found dirty")
          }
          if (w.name == query) {
            wordFound = true
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "searchResponse: ${e.localizedMessage}")
      }
    }
    if (!wordFound) wordList.add(Word.getNewWord(name = query))
    setSearchWords(wordList)
  }

  fun postComment(comment: Comment) {
    if (!checkNetworkAndShowError()) {
      return
    }
    comment.localStatus = true
    repository.postComment(
      comment = comment,
      success = {
        setRefreshComment(true)
      },
      failure = {
        setUserMessage(getString(R.string.error_unable_to_process))
      },
      connectionStatus = {

      }
    )
  }

  fun deleteComment(
    comment: Comment
  ) {
    if (!checkNetworkAndShowError()) {
      return
    }
    repository.deleteComment(
      comment = comment,
      success = {
        setRefreshComment(true)
      },
      failure = {
        setUserMessage(getString(R.string.error_unable_to_process))
      },
      connectionStatus = {

      }
    )
  }

  fun likeComment(
    comment: Comment,
    like: Boolean,
    userId: String,
    callback: () -> Unit
  ) {
    if (!checkNetworkAndShowError()) {
      return
    }
    repository.likeComment(
      comment = comment,
      like = like,
      userId = userId,
      success = callback,
      failure = {
        setUserMessage(getString(R.string.error_unable_to_process))
      },
      connectionStatus = {

      }
    )
  }

  // endregion

  sealed class FetchMode {
    object Popular : FetchMode()
    object Recent : FetchMode()
  }

  companion object {
    private const val TAG = "MainViewModel"
  }
}
