package com.tarripoha.android.domain.repository.user

import com.tarripoha.android.domain.entity.User

interface UserDataSource {

    suspend fun createUser(user: User)

    suspend fun getUser(phone: String): User?
}