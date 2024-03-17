package com.tarripoha.android.domain.usecase.dashboard

import com.tarripoha.android.domain.repository.dashboard.DashboardRepository

class GetDashboardData(private val repository: DashboardRepository){
    suspend operator fun invoke() = repository.get()
}
