package com.tarripoha.android.domain.usecases.word

import com.tarripoha.android.domain.repository.WordRepository

class GetAllWord(private val wordRepository: WordRepository){
    suspend operator fun invoke() = wordRepository.getAll()
} 