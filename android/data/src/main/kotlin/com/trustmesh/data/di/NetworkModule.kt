package com.trustmesh.data.di

import com.trustmesh.data.local.AuthPreferences
import com.trustmesh.data.remote.TrustMeshApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/" // Emulators use 10.0.2.2 to map to local server

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        prefs: AuthPreferences
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Section 8: Certificate pinning on the OkHttp client for production
        val certificatePinner = CertificatePinner.Builder()
            .add("trustmesh.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = prefs.getAccessToken()
                if (token != null && chain.request().header("No-Auth") == null) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideTrustMeshApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): TrustMeshApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TrustMeshApi::class.java)
    }
}
