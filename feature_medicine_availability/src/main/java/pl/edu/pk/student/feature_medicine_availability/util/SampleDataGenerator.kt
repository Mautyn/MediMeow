package pl.edu.pk.student.feature_medicine_availability.util

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pl.edu.pk.student.feature_medicine_availability.domain.models.MedicineCategory

object SampleDataGenerator {

    suspend fun generateSampleData(firestore: FirebaseFirestore) {
        val medicines = listOf(
            mapOf(
                "barcode" to "5909990960286",
                "name" to "Apap Extra",
                "manufacturer" to "US Pharmacia",
                "dosage" to "500mg + 65mg, 8 tabletek",
                "activeSubstance" to "Paracetamolum, Coffeinum",
                "category" to MedicineCategory.PAINKILLER.name
            ),
            mapOf(
                "barcode" to "5909991255527",
                "name" to "Ibuprom Max",
                "manufacturer" to "US Pharmacia",
                "dosage" to "400mg, 24 tabletki",
                "activeSubstance" to "Ibuprofenum",
                "category" to MedicineCategory.PAINKILLER.name
            ),
            mapOf(
                "barcode" to "5909997024936",
                "name" to "Rutinoscorbin",
                "manufacturer" to "Aflofarm",
                "dosage" to "100 tabletek",
                "activeSubstance" to "Acidum ascorbicum, Rutosidum",
                "category" to MedicineCategory.VITAMINS.name
            )
        )

        medicines.forEach { medicine ->
            firestore.collection("medicines").add(medicine).await()
        }

        val pharmacies = listOf(
            mapOf(
                "name" to "Apteka Pod Orłem",
                "address" to "ul. Mariacka 5",
                "city" to "Katowice",
                "postalCode" to "40-014",
                "latitude" to 50.2599,
                "longitude" to 19.0216,
                "phoneNumber" to "+48 32 253 45 67",
                "verified" to true,
                "openingHours" to mapOf(
                    "Poniedziałek-Piątek" to "8:00-20:00",
                    "Sobota" to "9:00-15:00",
                    "Niedziela" to "Nieczynne"
                )
            ),
            mapOf(
                "name" to "Apteka Centrum",
                "address" to "ul. 3 Maja 15",
                "city" to "Katowice",
                "postalCode" to "40-096",
                "latitude" to 50.2649,
                "longitude" to 19.0238,
                "phoneNumber" to "+48 32 258 12 34",
                "verified" to true,
                "openingHours" to mapOf(
                    "Poniedziałek-Piątek" to "7:30-21:00",
                    "Sobota" to "8:00-16:00",
                    "Niedziela" to "10:00-14:00"
                )
            )
        )

        pharmacies.forEach { pharmacy ->
            firestore.collection("pharmacies").add(pharmacy).await()
        }

        println("Sample data generated successfully!")
    }
}