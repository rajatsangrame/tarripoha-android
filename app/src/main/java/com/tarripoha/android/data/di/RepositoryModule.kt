package com.tarripoha.android.data.di

import android.content.Context
import android.content.res.Resources
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.data.datasource.home.FirebaseDashboardDataSource
import com.tarripoha.android.data.datasource.word.FirebaseWordDataSource
import com.tarripoha.android.data.datasource.home.HomeUseCases
import com.tarripoha.android.data.datasource.user.FirebaseUserDataSource
import com.tarripoha.android.data.datasource.user.UserUseCases
import com.tarripoha.android.domain.repository.dashboard.DashboardRepository
import com.tarripoha.android.domain.repository.user.UserRepository
import com.tarripoha.android.domain.repository.word.WordRepository
import com.tarripoha.android.domain.usecase.dashboard.GetDashboardData
import com.tarripoha.android.domain.usecase.user.CreateUser
import com.tarripoha.android.domain.usecase.user.GetUser
import com.tarripoha.android.domain.usecase.word.GetAllWord
import com.tarripoha.android.domain.usecase.word.GetFilteredWords
import com.tarripoha.android.domain.usecase.word.GetWordDetail
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
    @Singleton
    fun provideDashboardRepository(): DashboardRepository {
        val ref = Firebase.database.getReference("dashboard")
        return DashboardRepository(FirebaseDashboardDataSource(ref))
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        val ref = Firebase.firestore.collection("user")
        return UserRepository(FirebaseUserDataSource(ref))
    }

    @Provides
    fun provideResource(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    @Singleton
    fun provideUserUseCases(
        userRepository: UserRepository,
    ): UserUseCases {
        return UserUseCases(
            CreateUser(userRepository),
            GetUser(userRepository)
        )
    }

    @Provides
    @Singleton
    fun provideHomeUseCases(
        wordRepository: WordRepository,
        dashboardRepository: DashboardRepository
    ): HomeUseCases {
        return HomeUseCases(
            GetWordDetail(wordRepository),
            GetAllWord(wordRepository),
            GetDashboardData(dashboardRepository),
            GetFilteredWords(wordRepository)
        )
    }
}
