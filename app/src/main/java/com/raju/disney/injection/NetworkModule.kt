package com.raju.disney.injection

import com.google.gson.GsonBuilder
import com.raju.disney.BuildConfig
import com.raju.disney.api.BookApi
import com.raju.disney.api.FLIGHT_BASE_URL
import com.raju.disney.api.FlightApi
import com.raju.disney.opentelemetry.DisneyOTel
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
import javax.inject.Named

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
    ): BookApi {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(gsonConverterFactory)
                .callFactory(DisneyOTel.instance.createRumOkHttpCallFactory(httpClient))
                .client(httpClient)
                .build()
        return retrofit.create(BookApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFlightApi(
        httpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): FlightApi {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(FLIGHT_BASE_URL)
                .addConverterFactory(gsonConverterFactory)
                .client(httpClient)
                .callFactory(DisneyOTel.instance.createRumOkHttpCallFactory(httpClient))
                .build()
        return retrofit.create(FlightApi::class.java)
    }
}
