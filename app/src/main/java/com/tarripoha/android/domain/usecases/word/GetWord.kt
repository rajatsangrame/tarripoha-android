package com.tarripoha.android.domain.usecases.word

import com.tarripoha.android.domain.repository.WordRepository

class GetWord(private val wordRepository: WordRepository){
    suspend operator fun invoke(id: Long) = wordRepository.getWord(id)
} 