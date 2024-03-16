package com.tarripoha.android.domain.repository.dashboard

import com.tarripoha.android.data.model.DashboardResponse

interface DashboardDataSource {
    suspend fun get(): DashboardResponse
}
