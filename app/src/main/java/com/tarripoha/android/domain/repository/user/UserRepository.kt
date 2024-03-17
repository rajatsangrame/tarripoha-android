package com.tarripoha.android.domain.repository.user

import com.tarripoha.android.domain.entity.User

class UserRepository(private val dataSource: UserDataSource) {
    suspend fun createUser(user: User) = dataSource.createUser(user)
    suspend fun getUser(phone: String) = dataSource.getUser(phone)
}