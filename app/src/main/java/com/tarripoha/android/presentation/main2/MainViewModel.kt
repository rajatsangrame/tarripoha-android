package com.tarripoha.android.presentation.main2

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tarripoha.android.Constants.DashboardViewType
import com.tarripoha.android.Constants
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.repository.home.HomeUseCase
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordRepository
import com.tarripoha.android.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        viewModelScope.launch {
            val words = homeUseCase.getAllWord()
            Timber.tag(TAG).d("getAllWords: %s", words.size)
        }
    }

    private suspend fun fetchWords(category: String, lang: String): List<Word> {
        val words = when (category) {

            Constants.DashboardViewCategory.MOST_LIKED.value -> {
                homeUseCase.getFilteredWords(
                    WordRepository.FilterParams(
                        field = "lang",
                        value = lang,
                        sortField = "likes",
                        asc = false,
                        limit = 5
                    )
                )
            }

            Constants.DashboardViewCategory.MOST_VIEWED.value -> {
                homeUseCase.getFilteredWords(
                    WordRepository.FilterParams(
                        field = "lang",
                        value = lang,
                        sortField = "views",
                        asc = false,
                        limit = 5
                    )
                )
            }

            else -> null
        }
        return words ?: mutableListOf()
    }

    fun fetchDashboardWord() {
        viewModelScope.launch(Dispatchers.Main) {
            isRefreshing.value = true
            val dashBoardInfo = async(Dispatchers.IO) {
                val dashboardResponse = homeUseCase.dashboardData()
                val map = mutableMapOf<String, List<Word>>()
                dashboardResponse.labeledViews.forEach {
                    if (it.type == DashboardViewType.TYPE_WORD.value) {
                        val key = "${it.lang!!}_${it.category!!}"
                        val lang = Constants.getLanguageName(it.lang)!!
                        map[key] = fetchWords(it.category, lang)
                    }
                }
                DashBoardInfo(dashboardResponse, map)
            }.await()
            isRefreshing.value = false
            dashBoardInfoLiveData.value = dashBoardInfo
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}
