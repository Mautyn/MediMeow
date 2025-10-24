package pl.edu.pk.student.feature_auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.pk.student.feature_auth.data.AuthFirebaseRepository
import pl.edu.pk.student.feature_auth.data.AuthRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthEventModule {
    @Binds
    abstract fun bindAuthRepository(
        firebaseAuthRepository: AuthFirebaseRepository
    ): AuthRepository
}