package pl.edu.pk.student.feature_medicine_availability.di

import android.content.Context
import android.location.Geocoder
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.edu.pk.student.feature_medicine_availability.data.repository.MedicineAvailabilityRepository
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MedicineAvailabilityModule {

    @Provides
    @Singleton
    fun provideGeocoder(
        @ApplicationContext context: Context
    ): Geocoder {
        return Geocoder(context, Locale("pl", "PL"))
    }

    @Provides
    @Singleton
    fun provideMedicineAvailabilityRepository(
        firestore: FirebaseFirestore,
        geocoder: Geocoder
    ): MedicineAvailabilityRepository {
        return MedicineAvailabilityRepository(firestore, geocoder)
    }
}