package kr.ac.anu.mumu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.anu.mumu.data.repository.JoinRepositoryImpl
import kr.ac.anu.mumu.data.repository.LoginRepositoryImpl
import kr.ac.anu.mumu.data.repository.MyRepositoryImpl
import kr.ac.anu.mumu.domain.repository.JoinRepository
import kr.ac.anu.mumu.domain.repository.LoginRepository
import kr.ac.anu.mumu.domain.repository.MyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
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

    @Binds
    @Singleton
    abstract fun  bindMyRepository(
        myRepositoryImpl: MyRepositoryImpl
    ): MyRepository
}
