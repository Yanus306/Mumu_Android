package kr.ac.anu.mumu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.anu.mumu.data.repository.AnalysisRepositoryImpl
import kr.ac.anu.mumu.data.repository.HistoryRepositoryImpl
import kr.ac.anu.mumu.data.repository.JoinRepositoryImpl
import kr.ac.anu.mumu.data.repository.LoginRepositoryImpl
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import kr.ac.anu.mumu.domain.repository.HistoryRepository
import kr.ac.anu.mumu.domain.repository.JoinRepository
import kr.ac.anu.mumu.domain.repository.LoginRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        analysisRepositoryImpl: AnalysisRepositoryImpl
    ): AnalysisRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository

    @Binds
    @Singleton
    abstract fun bindJoinRepository(
        joinRepositoryImpl: JoinRepositoryImpl
    ): JoinRepository
}
