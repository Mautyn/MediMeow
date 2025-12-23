package pl.edu.pk.student.feature_interactions.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.pk.student.feature_interactions.data.remote.OpenFdaApiService
import pl.edu.pk.student.feature_interactions.data.repository.InteractionsRepository
import pl.edu.pk.student.feature_interactions.data.repository.InteractionsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InteractionsModule {

    @Provides
    @Singleton
    fun provideOpenFdaApiService(): OpenFdaApiService {
        return OpenFdaApiService()
    }

    @Provides
    @Singleton
    fun provideInteractionsRepository(
        apiService: OpenFdaApiService
    ): InteractionsRepository {
        return InteractionsRepositoryImpl(apiService)
    }
}