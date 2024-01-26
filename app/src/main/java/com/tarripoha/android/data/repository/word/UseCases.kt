package com.tarripoha.android.data.repository.word

import com.tarripoha.android.domain.usecase.word.AddWord
import com.tarripoha.android.domain.usecase.word.GetAllWord
import com.tarripoha.android.domain.usecase.word.GetWord
import com.tarripoha.android.domain.usecase.word.RemoveWord

data class UseCases(
    val addWord: AddWord,
    val getWord: GetWord,
    val getAllWord: GetAllWord,
    val removeWord: RemoveWord
)
