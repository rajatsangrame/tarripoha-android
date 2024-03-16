package com.tarripoha.android.data.datasource.user

import com.google.firebase.firestore.CollectionReference
import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.domain.repository.user.UserDataSource
import com.tarripoha.android.util.helper.FirebaseHelper.CloudStore.execute
import com.tarripoha.android.util.helper.FirebaseHelper.CloudStore.findOne

class FirebaseUserDataSource(
    private val reference: CollectionReference
) : UserDataSource {

    override suspend fun createUser(user: User) {
        reference.document(user.phone).set(user).execute()
    }

    override suspend fun getUser(phone: String): User {
        return reference.document(phone).get().findOne<User>()
    }
}