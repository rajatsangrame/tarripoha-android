package com.tarripoha.android.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.BaseViewModel
import com.tarripoha.android.R
import com.tarripoha.android.firebase.DashboardResponse
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */

class MainViewModel @Inject constructor(
    private val repository: Repository,
    app: Application
) : BaseViewModel(app) {

    private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    private val words: MutableLiveData<List<Word>> = MutableLiveData()
    private val searchWords: MutableLiveData<List<Word>> = MutableLiveData()
    private val query: MutableLiveData<String> = MutableLiveData()
    private val wordCount: MutableLiveData<Int> = MutableLiveData()
    private val dashboardData: MutableLiveData<MutableMap<String, MutableList<Word>>> =
        MutableLiveData()

    fun getWordCount() = wordCount

    fun isRefreshing() = isRefreshing

    fun getAllWords() = words

    fun getQuery() = query

    fun getDashboardData() = dashboardData

    fun setDashboardData(dashboardData: MutableMap<String, MutableList<Word>>?) {
        this.dashboardData.value = dashboardData
    }

    fun setQuery(query: String?) {
        this.query.value = query
    }

    fun getSearchWords() = searchWords

    fun setSearchWords(words: List<Word>?) {
        searchWords.value = words
    }

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

    fun fetchAllWord(dashboardResponseList: List<DashboardResponse>) {
        if (!checkNetworkAndShowError()) {
            return
        }
        isRefreshing.value = true
        repository.fetchAllWords(
            success = { snapshot ->
                fetchAllResponse(snapshot = snapshot, dashboardResponseList = dashboardResponseList)
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
        dashboardResponseList: List<DashboardResponse>,
        snapshot: DataSnapshot
    ) {
        val mapResponse: Map<String, DashboardResponse> =
            dashboardResponseList.filter { it.key != null && it.type == GlobalVar.TYPE_WORD }
                .associateBy { it.key!! }
        val tempMap: MutableMap<String, MutableList<Word>> = mutableMapOf()
        snapshot.children.forEach { snap ->
            try {
                if (snap.getValue(Word::class.java) != null) {
                    val word: Word = snap.getValue(Word::class.java)!!
                    if (!word.isDirty() && word.isApproved()) {
                        mapResponse.forEach { item ->
                            if (item.value.lang == GlobalVar.LANG_MR &&
                                word.lang == getString(R.string.marathi)
                            ) {
                                val list: MutableList<Word> = tempMap[item.key] ?: mutableListOf()
                                list.add(word)
                                tempMap[item.key] = list
                            } else if (item.value.lang == GlobalVar.LANG_HI &&
                                word.lang == getString(R.string.hindi)
                            ) {
                                val list: MutableList<Word> = tempMap[item.key] ?: mutableListOf()
                                list.add(word)
                                tempMap[item.key] = list
                            }
                        }
                    } else {
                        Log.i(TAG, "fetchAllResponse: ${word.name} found dirty")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
            }
        }

        mapResponse.forEach {
            if (it.value.category == GlobalVar.CATEGORY_TOP_LIKED) {
                val list: List<Word> = tempMap[it.key] ?: mutableListOf()
                if (it.value.category == GlobalVar.CATEGORY_TOP_LIKED) {
                    val sortedList = list.sortedWith { o1, o2 ->
                        var l2 = 0
                        o2.likes?.forEach {
                            if (it.value) l2++
                        }
                        var l1 = 0
                        o1.likes?.forEach {
                            if (it.value) l1++
                        }
                        l2 - l1
                    }
                    val top5: MutableList<Word> = mutableListOf()
                    for (i in 0..4) {
                        top5.add(sortedList[i])
                    }
                    tempMap[it.key] = top5
                }
            } else if (it.value.category == GlobalVar.CATEGORY_MOST_VIEWED) {

            }
        }
        setDashboardData(tempMap)
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
                    if (!w.isDirty()) {
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

    fun updateViewsCount(word: Word) {
        val user = getPrefUser()
        if (user?.id == null || GlobalVar.DEBUG_MODE) {
            Log.i(
                TAG,
                "updateViewsCount: ignored user not logged in / debug build ${GlobalVar.DEBUG_MODE}"
            )
            return
        }
        val viewsMap = word.views ?: mutableMapOf()
        val views = viewsMap[user.id] ?: mutableListOf()
        views.add(System.currentTimeMillis())
        updateViewsCount(
            word = word,
            views = views,
            userId = user.id,
            callback = {
                // no-op
            }
        )
    }

    private fun updateViewsCount(
        word: Word,
        views: MutableList<Long>,
        userId: String,
        callback: () -> Unit
    ) {
        if (!checkNetworkAndShowError()) {
            return
        }
        repository.updateViewsCount(
            word = word,
            views = views,
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

    companion object {
        private const val TAG = "MainViewModel"
    }
}
