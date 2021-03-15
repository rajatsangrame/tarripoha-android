package com.tarripoha.android.ui.main

import android.app.Application
import android.os.Handler
import androidx.lifecycle.*
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.BaseViewModel
import com.tarripoha.android.R
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

  // Jugaad. to avoid first network error. Firebase take initial time to set up.
  // Remove when Launch Screen is completed
  private var firstRun = true

  fun isRefreshing() = isRefreshing

  fun getAllWords() = words

  fun addWord(word: Word) {
    isRefreshing.value = true
    repository.addWord(word,
        success = {
          isRefreshing.value = false
          setUserMessage(getString(R.string.succ_data_added))
        },
        failure = {
          isRefreshing.value = false
          setUserMessage(getString(R.string.error_unable_to_process))
        }
    )
  }

  private fun checkFirebaseConnection() {
    if (firstRun) {
      Handler().postDelayed({
        firstRun = false
        checkFirebaseConnection()
      }, 2000)
    } else {
      repository.checkFirebaseConnection(
          success = {
          },
          failure = {
            setUserMessage(getString(R.string.error_no_internet))
            isRefreshing.value = false
          }
      )
    }
  }

  fun fetchAllWord() {
    checkFirebaseConnection()
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
          isRefreshing.value = false
        },
        failure = {
          isRefreshing.value = false
          setUserMessage(getString(R.string.error_unable_to_fetch))
        }
    )
  }

  companion object {
    private const val TAG = "MainViewModel"
  }
}