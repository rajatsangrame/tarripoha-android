package com.tarripoha.android.domain.usecase.word

import com.tarripoha.android.data.datasource.params.CloudStoreFilterParams
import com.tarripoha.android.domain.repository.word.WordRepository

class GetFilteredWords(private val wordRepository: WordRepository) {
    suspend operator fun invoke(params: CloudStoreFilterParams) =
        wordRepository.getFilteredWords(params)
}
