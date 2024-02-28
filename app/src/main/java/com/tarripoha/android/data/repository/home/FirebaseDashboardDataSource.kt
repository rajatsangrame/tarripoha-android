package com.tarripoha.android.data.repository.home


import com.google.firebase.database.DatabaseReference
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.domain.repository.dashboard.DashboardDataSource
import com.tarripoha.android.util.helper.FirebaseHelper.databaseFind

class FirebaseDashboardDataSource(private val reference: DatabaseReference) : DashboardDataSource {
    override suspend fun get(): DashboardResponse {
        return reference.databaseFind()
    }

}
