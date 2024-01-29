package com.tarripoha.android.domain.usecase.word

import com.tarripoha.android.domain.repository.word.WordRepository

class GetWordDetail(private val wordRepository: WordRepository){
    suspend operator fun invoke(id: Long) = wordRepository.getWordDetail(id)
}
