package com.tarripoha.android.domain.usecases.word

import com.tarripoha.android.domain.entities.Word
import com.tarripoha.android.domain.repository.WordRepository

class RemoveWord(private val wordRepository: WordRepository) {
    suspend operator fun invoke(word: Word) = wordRepository.removeWord(word)
}