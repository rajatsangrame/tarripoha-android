package com.tarripoha.android.domain.repository.dashboard


class DashboardRepository(private val dataSource: DashboardDataSource) {
    suspend fun get() = dataSource.get()
}
