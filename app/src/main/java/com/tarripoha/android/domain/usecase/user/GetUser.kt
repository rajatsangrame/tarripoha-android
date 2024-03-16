package com.tarripoha.android.domain.usecase.user

import com.tarripoha.android.domain.entity.User
import com.tarripoha.android.domain.repository.user.UserRepository

class GetUser(private val repository: UserRepository) {
    suspend operator fun invoke(phone: String) = repository.getUser(phone)
}
