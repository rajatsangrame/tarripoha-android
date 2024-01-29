package com.tarripoha.android.data.di

import android.content.Context
import android.content.res.Resources
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.data.repository.word.FirebaseWordDataSource
import com.tarripoha.android.data.repository.word.UseCases
import com.tarripoha.android.domain.repository.WordRepository
import com.tarripoha.android.domain.usecase.word.AddWord
import com.tarripoha.android.domain.usecase.word.GetAllWord
import com.tarripoha.android.domain.usecase.word.GetWord
import com.tarripoha.android.domain.usecase.word.RemoveWord
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWordRepository(@ApplicationContext context: Context): WordRepository {
        val ref = Firebase.firestore.collection("word")
        return WordRepository(FirebaseWordDataSource(ref))
    }

    @Provides
    fun provideResource(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    @Singleton
    fun provideWordUseCases(repository: WordRepository): UseCases {
        return UseCases(
            AddWord(repository),
            GetWord(repository),
            GetAllWord(repository),
            RemoveWord(repository)
        )
    }
}
