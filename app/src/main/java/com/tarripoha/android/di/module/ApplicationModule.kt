package com.tarripoha.android.di.module

import android.app.Application
import android.content.Context
import com.tarripoha.android.data.Repository
import com.tarripoha.android.data.db.WordDatabase
import com.tarripoha.android.data.rest.RetrofitApi
import com.tarripoha.android.di.ApplicationContext
import com.tarripoha.android.di.ApplicationScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
@Module(includes = [ViewModelModule::class, OkHttpClientModule::class])
class ApplicationModule(val application: Application) {

  @Provides
  fun retrofitApi(retrofit: Retrofit): RetrofitApi {
    return retrofit.create(RetrofitApi::class.java)
  }

  @ApplicationScope
  @Provides
  fun retrofit(
    okHttpClient: OkHttpClient,
    gsonConverterFactory: GsonConverterFactory
  ): Retrofit {
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(gsonConverterFactory)
        .build()
  }

  @Provides
  fun gsonConverterFactory(gson: Gson): GsonConverterFactory {
    return GsonConverterFactory.create(gson)
  }

  @Provides
  fun gson(): Gson {
    val gsonBuilder = GsonBuilder()
    return gsonBuilder.create()
  }

  @ApplicationScope
  @Provides
  fun getRepository(
    retrofitApi: RetrofitApi,
    database: WordDatabase?,
    context: Context
  ): Repository {
    return Repository(database, retrofitApi, context)
  }

  @ApplicationScope
  @Provides
  fun getDatabase(@ApplicationContext context: Context): WordDatabase? {
    return WordDatabase.getDataBase(context)
  }

  @ApplicationScope
  @Provides
  fun app(): Application {
    return application
  }

  companion object {
    const val BASE_URL: String = "https://itunes.apple.com"
  }
}