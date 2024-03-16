package com.tarripoha.android.domain.repository.dashboard
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.util.errorhandler.Result

interface DashboardDataSource {
    suspend fun get(): Result<DashboardResponse>
}
