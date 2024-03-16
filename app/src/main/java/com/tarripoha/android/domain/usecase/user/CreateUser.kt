package com.tarripoha.android.domain.usecase.user

import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.domain.repository.user.UserRepository

class CreateUser(private val repository: UserRepository) {
    suspend operator fun invoke(user: User) = repository.createUser(user)
}
