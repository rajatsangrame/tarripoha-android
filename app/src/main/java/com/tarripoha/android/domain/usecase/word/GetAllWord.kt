package com.tarripoha.android.domain.usecase.word

import com.tarripoha.android.domain.repository.WordRepository

class GetAllWord(private val wordRepository: WordRepository){
    suspend operator fun invoke() = wordRepository.getAll()
}
