package com.tarripoha.android.data.datasource.home

import com.tarripoha.android.domain.usecase.dashboard.GetDashboardData
import com.tarripoha.android.domain.usecase.word.GetAllWord
import com.tarripoha.android.domain.usecase.word.GetWordDetail
import com.tarripoha.android.domain.usecase.word.GetFilteredWords

data class HomeUseCase(
    val getWordDetail: GetWordDetail,
    val getAllWord: GetAllWord,
    val dashboardData: GetDashboardData,
    val getFilteredWords: GetFilteredWords,
)
