package kr.ac.anu.mumu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.anu.mumu.data.repository.AnalysisRepositoryImpl
import kr.ac.anu.mumu.data.repository.CommunityRepositoryImpl
import kr.ac.anu.mumu.data.repository.DiaryRepositoryImpl
import kr.ac.anu.mumu.data.repository.HistoryRepositoryImpl
import kr.ac.anu.mumu.data.repository.HospitalRepositoryImpl
import kr.ac.anu.mumu.data.repository.JoinRepositoryImpl
import kr.ac.anu.mumu.data.repository.LoginRepositoryImpl
import kr.ac.anu.mumu.data.repository.PetRepositoryImpl
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import kr.ac.anu.mumu.domain.repository.CommunityRepository
import kr.ac.anu.mumu.domain.repository.DiaryRepository
import kr.ac.anu.mumu.domain.repository.HistoryRepository
import kr.ac.anu.mumu.domain.repository.HospitalRepository
import kr.ac.anu.mumu.domain.repository.JoinRepository
import kr.ac.anu.mumu.domain.repository.LoginRepository
import kr.ac.anu.mumu.domain.repository.PetRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHospitalRepository(hospitalRepositoryImpl: HospitalRepositoryImpl): HospitalRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(diaryRepositoryImpl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(petRepositoryImpl: PetRepositoryImpl): PetRepository

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
    abstract fun bindCommunityRepository(
        communityRepositoryImpl: CommunityRepositoryImpl
    ): CommunityRepository

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
