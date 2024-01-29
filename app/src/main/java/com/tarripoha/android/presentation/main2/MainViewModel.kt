package com.tarripoha.android.presentation.main2

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tarripoha.android.Constants
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.repository.home.HomeUseCase
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordRepository
import com.tarripoha.android.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val homeUseCase: HomeUseCase,
    resources: Resources
) :
    BaseViewModel(resources) {

    class DashBoardInfo(
        val response: DashboardResponse,
        val data: Map<String, List<Word>>
    )

    private val dashBoardInfo = MutableLiveData<DashBoardInfo>()

    fun getDashBoardInfo(): LiveData<DashBoardInfo> = dashBoardInfo

    fun getAllWords() {
        viewModelScope.launch {
            val words = async { homeUseCase.getAllWord() }.await()
            Timber.tag(TAG).d("getAllWords: %s", words.size)
        }
    }

    fun fetchDashboardWord() {
        isRefreshing.value = true
        viewModelScope.launch {
            val dashboardResponse = async { homeUseCase.dashboardData() }.await()
            val map = mutableMapOf<String, List<Word>>()
            dashboardResponse.labeledViews.forEach {
                if (it.type == "word") {
                    val key = "${it.lang}_${it.category}"
                    val lang = Constants.getLanguageName(it.lang!!)!!
                    val words = when (it.category) {
                        Constants.DashboardViewCategory.MOST_LIKED.value -> {
                            async {
                                homeUseCase.getFilteredWords(
                                    WordRepository.FilterParams(
                                        field = "lang",
                                        value = lang,
                                        sortField = "likes",
                                        asc = false,
                                        limit = 5
                                    )
                                )
                            }.await()
                        }

                        Constants.DashboardViewCategory.MOST_VIEWED.value -> {
                            async {
                                homeUseCase.getFilteredWords(
                                    WordRepository.FilterParams(
                                        field = "lang",
                                        value = lang,
                                        sortField = "views",
                                        asc = false,
                                        limit = 5
                                    )
                                )
                            }.await()
                        }

                        else -> null
                    }
                    map[key] = words ?: mutableListOf()
                }
            }
            isRefreshing.postValue(false)
            dashBoardInfo.postValue(DashBoardInfo(response = dashboardResponse, data = map))
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}
