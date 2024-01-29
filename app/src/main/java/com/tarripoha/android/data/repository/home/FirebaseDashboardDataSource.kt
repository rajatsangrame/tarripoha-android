package com.tarripoha.android.data.repository.home

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.domain.repository.dashboard.DashboardDataSource
import com.tarripoha.android.util.ktx.FirebaseUtil.databaseFind

class FirebaseDashboardDataSource(private val reference: DatabaseReference) : DashboardDataSource {
    override suspend fun get(): DashboardResponse {
        val snapshot = reference.databaseFind()
        return parseData(snapshot)
    }

    private fun parseData(snapshot: DataSnapshot): DashboardResponse {
        return Gson().fromJson(snapshot.value.toString(), DashboardResponse::class.java)
    }

}
