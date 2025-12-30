package pl.edu.pk.student.feature_medical_records.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.edu.pk.student.feature_medical_records.data.remote.SupabaseStorageService
import pl.edu.pk.student.feature_medical_records.data.repository.MedicalRecordsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MedicalRecordsModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    @Provides
    @Singleton
    fun provideSupabaseStorageService(): SupabaseStorageService {
        return SupabaseStorageService()
    }

    @Provides
    @Singleton
    fun provideMedicalRecordsRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        @ApplicationContext context: Context,
        supabaseStorageService: SupabaseStorageService
    ): MedicalRecordsRepository {
        return MedicalRecordsRepository(
            firestore,
            auth,
            context,
            supabaseStorageService
        )
    }
}