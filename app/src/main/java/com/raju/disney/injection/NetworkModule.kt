package com.raju.disney.injection

import com.google.gson.GsonBuilder
import com.raju.disney.BuildConfig
import com.raju.disney.api.GiphyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

  @Provides
  fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
  }

  @Provides
  @Singleton
  fun provideHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    val builder =
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
    return builder.build()
  }

  @Provides
  @Singleton
  fun provideGsonConverterFactory(): GsonConverterFactory {
    return GsonConverterFactory.create(GsonBuilder().create())
  }

  @Provides
  @Singleton
  fun provideImagesApi(
      httpClient: OkHttpClient,
      gsonConverterFactory: GsonConverterFactory
  ): GiphyApi {
    val retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .client(httpClient)
            .build()
    return retrofit.create(GiphyApi::class.java)
  }
}
