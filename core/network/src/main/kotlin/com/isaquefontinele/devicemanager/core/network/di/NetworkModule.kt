package com.isaquefontinele.devicemanager.core.network.di

import com.isaquefontinele.devicemanager.core.network.AuthInterceptor
import com.isaquefontinele.devicemanager.core.network.BuildConfig
import com.isaquefontinele.devicemanager.core.network.DeviceApi
import com.isaquefontinele.devicemanager.core.network.DeviceRemoteDataSource
import com.isaquefontinele.devicemanager.core.network.RetrofitDeviceRemoteDataSource
import com.isaquefontinele.devicemanager.core.network.createDeviceApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {

    @Binds
    abstract fun bindDeviceRemoteDataSource(impl: RetrofitDeviceRemoteDataSource): DeviceRemoteDataSource

    companion object {
        private const val CONNECT_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 15L
        private const val CALL_TIMEOUT_SECONDS = 30L

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.API_TOKEN))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("Authorization")
                        },
                    )
                }
            }
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        @Provides
        @Singleton
        fun provideDeviceApi(okHttpClient: OkHttpClient, json: Json): DeviceApi =
            createDeviceApi(BuildConfig.API_BASE_URL, okHttpClient, json)
    }
}
