package com.tarripoha.android.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class MainActivityScope

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class LoginActivityScope

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class StartupActivityScope

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class ApplicationScope
