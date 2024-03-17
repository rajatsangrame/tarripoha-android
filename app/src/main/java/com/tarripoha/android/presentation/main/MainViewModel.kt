package com.tarripoha.android.presentation.main

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tarripoha.android.Constants.DashboardViewType
import com.tarripoha.android.Constants
import com.tarripoha.android.R
import com.tarripoha.android.data.datasource.params.CloudStoreFilterParams
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.datasource.home.HomeUseCases
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val homeUseCases: HomeUseCases,
    resources: Resources
) :
    BaseViewModel(resources) {

    class DashBoardInfo(
        val response: DashboardResponse,
        val data: Map<String, List<Word>>
    )

    private val dashBoardInfoLiveData = MutableLiveData<DashBoardInfo>()
    fun getDashBoardInfo(): LiveData<DashBoardInfo> = dashBoardInfoLiveData

    // region WordListFragment
    var wordListParam: WordListFragment.WordListFragmentParam? = null
    private val words: MutableLiveData<List<Word>?> = MutableLiveData()
    private val wordListErrorMsg: MutableLiveData<String?> = MutableLiveData()
    fun getWords(): LiveData<List<Word>?> = words
    fun getWordListErrorMsg(): LiveData<String?> = wordListErrorMsg
    // endregion


    fun getAllWords() {
        viewModelScope.launch(exceptionHandler) {
            showProgress.value = true
            val words = homeUseCases.getAllWord()
            showProgress.value = false
            Timber.tag(TAG).d("getAllWords: %s", words.size)
        }
    }

    fun fetchInitialWordList(
        swipeReload: Boolean = false
    ) {
        val param = wordListParam
        if (param == null) {
            setUserMessage(getString(R.string.error_unknown))
            return
        }
        viewModelScope.launch(exceptionHandler) {
            if (!swipeReload) showProgress.value = true
            val wordList = async(Dispatchers.IO) {
                fetchWords(
                    param.category,
                    param.lang,
                    limit = 20
                )
            }.await()
            words.value = wordList
            if (!swipeReload) showProgress.value = false
            isRefreshing.value = false
        }
    }

    private suspend fun fetchWords(category: String, lang: String, limit: Long = 10L): List<Word> {
        val filter = mutableMapOf<String, Any>().also {
            it["lang"] = lang
            it["dirty"] = false
            it["approved"] = true
        }
        val words = when (category) {
            Constants.DashboardViewCategory.MOST_LIKED.value -> {
                homeUseCases.getFilteredWords(
                    CloudStoreFilterParams(
                        data = filter,
                        sortField = "likes",
                        asc = false,
                        limit = limit
                    )
                )
            }

            Constants.DashboardViewCategory.MOST_VIEWED.value -> {
                homeUseCases.getFilteredWords(
                    CloudStoreFilterParams(
                        data = filter,
                        sortField = "views",
                        asc = false,
                        limit = limit
                    )
                )
            }

            else -> null
        }
        return words ?: mutableListOf()
    }

    fun fetchDashboardData() {
        viewModelScope.launch(exceptionHandler) {
            isRefreshing.value = true

            val map = mutableMapOf<String, List<Word>>()
            val dashboardResult = async(Dispatchers.IO) {
                homeUseCases.dashboardData()
            }.await()

            val deferred: List<Deferred<Unit>> =
                dashboardResult.labeledViews.filter {
                    it.type == DashboardViewType.TYPE_WORD.value
                }.map {
                    async(Dispatchers.IO) {
                        val key = "${it.lang!!}_${it.category!!}"
                        val lang = Constants.getLanguageName(it.lang)!!
                        map[key] = fetchWords(it.category, lang)
                    }
                }

            deferred.awaitAll()
            val dashBoardInfo = DashBoardInfo(dashboardResult, map)
            dashBoardInfoLiveData.value = dashBoardInfo
            isRefreshing.value = false
        }
    }

    fun resetWordListParams() {
        wordListParam = null
        words.value = null
        wordListErrorMsg.value = null
    }


    companion object {
        private const val TAG = "MainViewModel"
    }

}
