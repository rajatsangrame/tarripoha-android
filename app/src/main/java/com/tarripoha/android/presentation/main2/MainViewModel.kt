package com.tarripoha.android.presentation.main2

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarripoha.android.data.repository.word.UseCases
import com.tarripoha.android.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val useCases: UseCases, resources: Resources) :
    BaseViewModel(resources) {

    fun getAllWords() {
        viewModelScope.launch {
            val words = async { useCases.getAllWord() }.await()
            Timber.tag(TAG).d("getAllWords: %s", words.size)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}
