package com.vastavik.computer.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vastavik.computer.data.api.ApiConfig
import com.vastavik.computer.data.api.AuthInterceptor
import com.vastavik.computer.data.api.VastavikApiService
import com.vastavik.computer.data.repository.AuthRepository
import com.vastavik.computer.data.repository.FirestoreRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("vastavik_prefs", Context.MODE_PRIVATE)
    }
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = AuthRepository()

    @Provides
    @Singleton
    fun provideFirestoreRepository(): FirestoreRepository = FirestoreRepository()

    // ---- Backend API ----
    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideVastavikApiService(retrofit: Retrofit): VastavikApiService =
        retrofit.create(VastavikApiService::class.java)

    @Provides
    @Singleton
    fun provideVastavikApiRepository(api: VastavikApiService): VastavikApiRepository =
        VastavikApiRepository(api)
}