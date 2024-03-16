package com.tarripoha.android.presentation.main2

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tarripoha.android.Constants.DashboardViewType
import com.tarripoha.android.Constants
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.datasource.home.HomeUseCase
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordRepository
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
    private val homeUseCase: HomeUseCase,
    resources: Resources
) :
    BaseViewModel(resources) {

    class DashBoardInfo(
        val response: DashboardResponse,
        val data: Map<String, List<Word>>
    )

    private val dashBoardInfoLiveData = MutableLiveData<DashBoardInfo>()

    fun getDashBoardInfo(): LiveData<DashBoardInfo> = dashBoardInfoLiveData

    fun getAllWords() {
        viewModelScope.launch(Dispatchers.Main + exceptionHandler) {
            val words = homeUseCase.getAllWord()
            Timber.tag(TAG).d("getAllWords: %s", words.size)
        }
    }

    private suspend fun fetchWords(category: String, lang: String): List<Word> {
        val filter = mutableMapOf<String, Any>().also {
            it["lang"] = lang
            it["dirty"] = false
            it["approved"] = true
        }
        val words = when (category) {
            Constants.DashboardViewCategory.MOST_LIKED.value -> {
                homeUseCase.getFilteredWords(
                    WordRepository.FilterParams(
                        data = filter,
                        sortField = "likes",
                        asc = false,
                        limit = 10
                    )
                )
            }

            Constants.DashboardViewCategory.MOST_VIEWED.value -> {
                homeUseCase.getFilteredWords(
                    WordRepository.FilterParams(
                        data = filter,
                        sortField = "views",
                        asc = false,
                        limit = 10
                    )
                )
            }

            else -> null
        }
        return words ?: mutableListOf()
    }

    fun fetchDashboardData() {
        viewModelScope.launch(Dispatchers.Main + exceptionHandler) {
            isRefreshing.value = true

            val map = mutableMapOf<String, List<Word>>()
            val dashboardResult = async(Dispatchers.IO) {
                homeUseCase.dashboardData()
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

    companion object {
        private const val TAG = "MainViewModel"
    }

}
