package kr.ac.anu.mumu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.anu.mumu.data.datasource.AnalysisService
import kr.ac.anu.mumu.data.datasource.AuthService
import kr.ac.anu.mumu.data.datasource.CommunityService
import kr.ac.anu.mumu.data.datasource.DiaryService
import kr.ac.anu.mumu.data.datasource.HistoryService
import kr.ac.anu.mumu.data.datasource.JoinService
import kr.ac.anu.mumu.data.datasource.PetService
import kr.ac.anu.mumu.data.local.SessionAuthenticator
import kr.ac.anu.mumu.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(sessionManager: SessionManager, authenticator: SessionAuthenticator): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(authenticator)
            .addInterceptor { chain ->
                val token = sessionManager.accessToken
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Named("unauthenticated")
    fun provideUnauthenticatedRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://13.125.155.32:8080/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://13.125.155.32:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(@Named("unauthenticated") retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideJoinService(retrofit: Retrofit): JoinService {
        return retrofit.create(JoinService::class.java)
    }

    @Provides
    @Singleton
    fun provideAnalysisService(retrofit: Retrofit): AnalysisService {
        return retrofit.create(AnalysisService::class.java)
    }

    @Provides
    @Singleton
    fun providePetService(retrofit: Retrofit): PetService {
        return retrofit.create(PetService::class.java)
    }

    @Provides
    @Singleton
    fun provideDiaryService(retrofit: Retrofit): DiaryService = retrofit.create(DiaryService::class.java)

    @Provides
    @Singleton
    fun provideHistoryService(retrofit: Retrofit): HistoryService {
        return retrofit.create(HistoryService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommunityService(retrofit: Retrofit): CommunityService {
        return retrofit.create(CommunityService::class.java)
    }
}
