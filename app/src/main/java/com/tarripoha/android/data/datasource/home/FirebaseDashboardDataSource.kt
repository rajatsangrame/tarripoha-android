package com.tarripoha.android.data.datasource.home


import com.google.firebase.database.DatabaseReference
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.domain.repository.dashboard.DashboardDataSource
import com.tarripoha.android.util.errorhandler.Result
import com.tarripoha.android.util.helper.FirebaseHelper.RealtimeDatabase.findItems

class FirebaseDashboardDataSource(private val reference: DatabaseReference) : DashboardDataSource {
    override suspend fun get(): Result<DashboardResponse> {
        val response: DashboardResponse = reference.findItems()
        return Result.Success(response)
    }

}
