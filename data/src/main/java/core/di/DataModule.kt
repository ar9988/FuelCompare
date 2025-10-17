package core.di

import android.content.Context
import com.example.domain.repository.HomeRepository
import core.datasource.AAOSVehicleDataSource
import core.repository.HomeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideAAOSVehicleDataSource(
        @ApplicationContext context: Context
    ): AAOSVehicleDataSource {
        // AAOS 데이터 소스를 앱 전체에서 공유되는 싱글톤으로 제공
        return AAOSVehicleDataSource(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository
}