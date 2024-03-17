package com.tarripoha.android.data.datasource.user

import com.tarripoha.android.domain.usecase.user.CreateUser
import com.tarripoha.android.domain.usecase.user.GetUser

data class UserUseCases(
    val createUser: CreateUser,
    val getUser: GetUser
)
