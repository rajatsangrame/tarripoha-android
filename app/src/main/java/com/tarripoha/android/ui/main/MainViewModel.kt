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
import com.tarripoha.android.firebase.LabeledView
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

    // SearchFragment
    private val searchWords: MutableLiveData<List<Word>> = MutableLiveData()
    private val query: MutableLiveData<String> = MutableLiveData()
    private val chars: MutableLiveData<String> = MutableLiveData()

    // HomeFragment
    private val wordCount: MutableLiveData<Int> = MutableLiveData()
    private val dashboardData: MutableLiveData<MutableMap<String, MutableList<Word>>> =
        MutableLiveData()

    // WordListFragment
    private val wordListParam: MutableLiveData<WordListFragment.WordListFragmentParam> =
        MutableLiveData()
    private val words: MutableLiveData<List<Word>> = MutableLiveData()

    fun getWordCount() = wordCount

    private fun setWordCount(wordCount: Int?) {
        this.wordCount.value = wordCount
    }

    fun isRefreshing() = isRefreshing

    private fun setRefreshing(isRefreshing: Boolean?) {
        this.isRefreshing.value = isRefreshing
    }

    fun getWords() = words

    fun setWords(words: List<Word>?) {
        this.words.value = words
    }

    fun getQuery() = query

    fun setQuery(query: String?) {
        this.query.value = query
    }

    fun getDashboardData() = dashboardData

    fun getChar() = chars

    fun setChars(chars: String?) {
        this.chars.value = chars
    }

    private fun setDashboardData(dashboardData: MutableMap<String, MutableList<Word>>?) {
        this.dashboardData.value = dashboardData
    }

    fun getSearchWords() = searchWords

    fun setSearchWords(words: List<Word>?) {
        searchWords.value = words
    }

    fun getWordListParam() = wordListParam

    fun setWordListParam(wordListParam: WordListFragment.WordListFragmentParam?) {
        this.wordListParam.value = wordListParam
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

    fun fetchWords(param: WordListFragment.WordListFragmentParam) {
        if (!checkNetworkAndShowError()) {
            return
        }
        setRefreshing(true)
        repository.fetchAllWords(
            success = { snapshot ->
                fetchAllResponse(snapshot = snapshot, lang = param.lang, category = param.category)
            },
            failure = {
                setRefreshing(false)
                setUserMessage(getString(R.string.error_unable_to_fetch))
            },
            connectionStatus = {
                if (!it) setRefreshing(false)
            }
        )
    }

    private fun fetchAllResponse(
        snapshot: DataSnapshot,
        lang: String, category: String
    ) {
        val list: MutableList<Word> = mutableListOf()
        snapshot.children.forEach { snap ->
            try {
                val word: Word? = snap.getValue(Word::class.java)
                if (word != null && !word.isDirty() && word.isApproved()) {
                    if (word.lang == lang) {
                        list.add(word)
                    }
                } else {
                    Log.i(TAG, "fetchAllResponse: $word found dirty or not approved")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
            }
        }
        if (category == GlobalVar.CATEGORY_TOP_LIKED) {
            val sortedList = getTopLikedWords(list = list, total = 50)
            setWords(sortedList)
        } else if (category == GlobalVar.CATEGORY_MOST_VIEWED) {
            val sortedList = getMostViewedList(list = list, total = 50)
            setWords(sortedList)
        }

        setRefreshing(false)
    }

    fun fetchAllWord(labeledView: List<LabeledView>) {
        if (!checkNetworkAndShowError()) {
            setRefreshing(false)
            return
        }
        setRefreshing(true)
        repository.fetchAllWords(
            success = { snapshot ->
                fetchAllResponse(snapshot = snapshot, labeledViews = labeledView)
            },
            failure = {
                setRefreshing(false)
                setUserMessage(getString(R.string.error_unable_to_fetch))
            },
            connectionStatus = {
                if (!it) setRefreshing(false)
            }
        )
    }

    private fun fetchAllResponse(
        labeledViews: List<LabeledView>,
        snapshot: DataSnapshot
    ) {
        val mapResponse: Map<String, LabeledView> =
            labeledViews.filter { it.key != null && it.type == GlobalVar.TYPE_WORD }
                .associateBy { it.key!! }
        val tempMap: MutableMap<String, MutableList<Word>> = mutableMapOf()
        snapshot.children.forEach { snap ->
            try {
                val word: Word? = snap.getValue(Word::class.java)
                if (word != null && !word.isDirty() && word.isApproved()) {
                    mapResponse.forEach { item ->
                        if (item.value.lang == word.lang) {
                            tempMap.updateValue(item.key, word)
                        }
                    }
                } else {
                    Log.i(TAG, "fetchAllResponse: $word found dirty or not approved")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
            }
        }

        mapResponse.forEach {
            val list: MutableList<Word> = tempMap[it.key] ?: mutableListOf()
            if (it.value.category == GlobalVar.CATEGORY_TOP_LIKED) {
                tempMap[it.key] = getTopLikedWords(list)
            } else if (it.value.category == GlobalVar.CATEGORY_MOST_VIEWED) {
                tempMap[it.key] = getMostViewedList(list)
            }
        }
        setDashboardData(tempMap)
        setWordCount(snapshot.childrenCount.toInt())
        setRefreshing(false)
    }

    private fun MutableMap<String, MutableList<Word>>.updateValue(
        key: String,
        word: Word
    ) {
        val list: MutableList<Word> = this[key] ?: mutableListOf()
        list.add(word)
        this[key] = list
    }

    private fun getTopLikedWords(list: MutableList<Word>, total: Int = 5): MutableList<Word> {
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
        val max = if (list.size > total) total else list.size
        for (i in 0 until max) {
            top5.add(sortedList[i])
        }
        return top5
    }

    private fun getMostViewedList(list: MutableList<Word>, total: Int = 5): MutableList<Word> {
        val sortedList = list.sortedWith { o1, o2 ->
            val l2 = o2.views?.size ?: 0
            val l1 = o1.views?.size ?: 0
            l2 - l1
        }
        val top5: MutableList<Word> = mutableListOf()
        val max = if (list.size > total) total else list.size
        for (i in 0 until max) {
            top5.add(sortedList[i])
        }
        return top5
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
                    if (!w.isDirty() && w.isApproved()) {
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
