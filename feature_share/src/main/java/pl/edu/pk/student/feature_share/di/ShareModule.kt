package pl.edu.pk.student.feature_share.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.edu.pk.student.feature_medical_records.data.repository.MedicalRecordsRepository
import pl.edu.pk.student.feature_share.data.ShareRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShareModule {

    @Provides
    @Singleton
    fun provideShareRepository(
        @ApplicationContext context: Context,
        medicalRecordsRepository: MedicalRecordsRepository
    ): ShareRepository {
        return ShareRepository(context, medicalRecordsRepository)
    }
}