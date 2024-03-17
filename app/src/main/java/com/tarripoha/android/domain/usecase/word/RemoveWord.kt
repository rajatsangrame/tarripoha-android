package com.tarripoha.android.domain.usecase.word

import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.domain.repository.word.WordRepository

class RemoveWord(private val wordRepository: WordRepository) {
    suspend operator fun invoke(word: Word) = wordRepository.removeWord(word)
}
