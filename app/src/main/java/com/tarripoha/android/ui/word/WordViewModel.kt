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


    // region Variable

    private var fetchMode: FetchMode = FetchMode.Popular
    private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    private val wordDetail: MutableLiveData<Word> = MutableLiveData()
    private val refreshComment: MutableLiveData<Boolean> = MutableLiveData()

    // endregion

    fun setRefreshing(refresh: Boolean?) {
        isRefreshing.value = refresh
    }

    fun isRefreshing() = isRefreshing


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

    fun updateWord(word: Word) {
        if (!checkNetworkAndShowError()) {
            return
        }
        isRefreshing.value = true
        repository.addNewWord(
            word = word,
            success = {
                isRefreshing.value = false
                setWordDetail(word)
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
