package pl.edu.pk.student.feature_medicine_availability.data.repository

import android.location.Geocoder
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pl.edu.pk.student.feature_medicine_availability.domain.models.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class
MedicineAvailabilityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val geocoder: Geocoder
) {

    companion object {
        private const val TAG = "MedicineAvailRepo"
        private const val MEDICINES_COLLECTION = "medicines"
        private const val PHARMACIES_COLLECTION = "pharmacies"
        private const val STOCK_COLLECTION = "medicine_stock"
        private const val EARTH_RADIUS_KM = 6371.0
    }

    suspend fun searchMedicineByBarcode(barcode: String): Result<Medicine?> {
        return try {
            val snapshot = firestore.collection(MEDICINES_COLLECTION)
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .await()

            val medicine = snapshot.documents.firstOrNull()?.let { doc ->
                Medicine(
                    id = doc.id,
                    barcode = doc.getString("barcode") ?: "",
                    name = doc.getString("name") ?: "",
                    manufacturer = doc.getString("manufacturer") ?: "",
                    dosage = doc.getString("dosage") ?: "",
                    activeSubstance = doc.getString("activeSubstance") ?: "",
                    category = MedicineCategory.valueOf(
                        doc.getString("category") ?: MedicineCategory.OTHER.name
                    )
                )
            }

            Result.success(medicine)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching medicine by barcode", e)
            Result.failure(e)
        }
    }

    suspend fun searchMedicineByName(query: String): Result<List<Medicine>> {
        return try {
            val snapshot = firestore.collection(MEDICINES_COLLECTION)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val medicines = snapshot.documents.mapNotNull { doc ->
                try {
                    Medicine(
                        id = doc.id,
                        barcode = doc.getString("barcode") ?: "",
                        name = doc.getString("name") ?: "",
                        manufacturer = doc.getString("manufacturer") ?: "",
                        dosage = doc.getString("dosage") ?: "",
                        activeSubstance = doc.getString("activeSubstance") ?: "",
                        category = MedicineCategory.valueOf(
                            doc.getString("category") ?: MedicineCategory.OTHER.name
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing medicine document", e)
                    null
                }
            }

            Result.success(medicines)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching medicines by name", e)
            Result.failure(e)
        }
    }

    suspend fun getLocationFromCity(city: String): Result<Location?> {
        return try {
            val addresses = geocoder.getFromLocationName("$city, Polska", 1)

            val location = addresses?.firstOrNull()?.let {
                Location(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }

            Result.success(location)
        } catch (e: Exception) {
            Log.e(TAG, "Error geocoding city", e)
            Result.failure(e)
        }
    }

    suspend fun findNearbyPharmacies(
        location: Location,
        radiusKm: Double = 10.0
    ): Result<List<Pharmacy>> {
        return try {
            val latDelta = radiusKm / 111.0
            val lonDelta = radiusKm / (111.0 * cos(Math.toRadians(location.latitude)))

            val snapshot = firestore.collection(PHARMACIES_COLLECTION)
                .whereGreaterThan("latitude", location.latitude - latDelta)
                .whereLessThan("latitude", location.latitude + latDelta)
                .get()
                .await()

            val pharmacies = snapshot.documents.mapNotNull { doc ->
                try {
                    val pharmacy = Pharmacy(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        address = doc.getString("address") ?: "",
                        city = doc.getString("city") ?: "",
                        postalCode = doc.getString("postalCode") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        openingHours = (doc.get("openingHours") as? Map<String, String>) ?: emptyMap(),
                        verified = doc.getBoolean("verified") ?: false
                    )

                    val distance = calculateDistance(
                        location.latitude, location.longitude,
                        pharmacy.latitude, pharmacy.longitude
                    )

                    if (distance <= radiusKm) pharmacy else null
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing pharmacy document", e)
                    null
                }
            }.sortedBy { pharmacy ->
                calculateDistance(
                    location.latitude, location.longitude,
                    pharmacy.latitude, pharmacy.longitude
                )
            }

            Result.success(pharmacies)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding nearby pharmacies", e)
            Result.failure(e)
        }
    }

    suspend fun checkMedicineAvailability(
        medicineId: String,
        pharmacyIds: List<String>
    ): Result<List<PharmacyWithStock>> {
        return try {
            if (pharmacyIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val stockSnapshot = firestore.collection(STOCK_COLLECTION)
                .whereEqualTo("medicineId", medicineId)
                .whereIn("pharmacyId", pharmacyIds.take(10))
                .whereEqualTo("available", true)
                .get()
                .await()

            val stockByPharmacy = stockSnapshot.documents.mapNotNull { doc ->
                try {
                    MedicineStock(
                        id = doc.id,
                        medicineId = doc.getString("medicineId") ?: "",
                        pharmacyId = doc.getString("pharmacyId") ?: "",
                        barcode = doc.getString("barcode") ?: "",
                        quantity = doc.getLong("quantity")?.toInt() ?: 0,
                        price = doc.getDouble("price") ?: 0.0,
                        lastUpdated = doc.getLong("lastUpdated") ?: 0L,
                        available = doc.getBoolean("available") ?: false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing stock document", e)
                    null
                }
            }

            val pharmaciesWithStock = stockByPharmacy.mapNotNull { stock ->
                try {
                    val pharmacyDoc = firestore.collection(PHARMACIES_COLLECTION)
                        .document(stock.pharmacyId)
                        .get()
                        .await()

                    val medicineDoc = firestore.collection(MEDICINES_COLLECTION)
                        .document(stock.medicineId)
                        .get()
                        .await()

                    val pharmacy = Pharmacy(
                        id = pharmacyDoc.id,
                        name = pharmacyDoc.getString("name") ?: "",
                        address = pharmacyDoc.getString("address") ?: "",
                        city = pharmacyDoc.getString("city") ?: "",
                        postalCode = pharmacyDoc.getString("postalCode") ?: "",
                        latitude = pharmacyDoc.getDouble("latitude") ?: 0.0,
                        longitude = pharmacyDoc.getDouble("longitude") ?: 0.0,
                        phoneNumber = pharmacyDoc.getString("phoneNumber") ?: "",
                        openingHours = (pharmacyDoc.get("openingHours") as? Map<String, String>) ?: emptyMap(),
                        verified = pharmacyDoc.getBoolean("verified") ?: false
                    )

                    val medicine = Medicine(
                        id = medicineDoc.id,
                        barcode = medicineDoc.getString("barcode") ?: "",
                        name = medicineDoc.getString("name") ?: "",
                        manufacturer = medicineDoc.getString("manufacturer") ?: "",
                        dosage = medicineDoc.getString("dosage") ?: "",
                        activeSubstance = medicineDoc.getString("activeSubstance") ?: "",
                        category = MedicineCategory.valueOf(
                            medicineDoc.getString("category") ?: MedicineCategory.OTHER.name
                        )
                    )

                    PharmacyWithStock(
                        pharmacy = pharmacy,
                        stock = stock,
                        medicine = medicine
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading pharmacy/medicine details", e)
                    null
                }
            }

            Result.success(pharmaciesWithStock)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking medicine availability", e)
            Result.failure(e)
        }
    }

    fun observeMedicineStock(
        medicineId: String,
        pharmacyId: String
    ): Flow<MedicineStock?> = callbackFlow {
        val listener = firestore.collection(STOCK_COLLECTION)
            .whereEqualTo("medicineId", medicineId)
            .whereEqualTo("pharmacyId", pharmacyId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing stock", error)
                    close(error)
                    return@addSnapshotListener
                }

                val stock = snapshot?.documents?.firstOrNull()?.let { doc ->
                    try {
                        MedicineStock(
                            id = doc.id,
                            medicineId = doc.getString("medicineId") ?: "",
                            pharmacyId = doc.getString("pharmacyId") ?: "",
                            barcode = doc.getString("barcode") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 0,
                            price = doc.getDouble("price") ?: 0.0,
                            lastUpdated = doc.getLong("lastUpdated") ?: 0L,
                            available = doc.getBoolean("available") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing stock", e)
                        null
                    }
                }

                trySend(stock)
            }

        awaitClose { listener.remove() }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    suspend fun updateMedicineStock(
        pharmacyId: String,
        medicineId: String,
        barcode: String,
        quantity: Int,
        price: Double
    ): Result<Unit> {
        return try {
            val stockData = hashMapOf(
                "medicineId" to medicineId,
                "pharmacyId" to pharmacyId,
                "barcode" to barcode,
                "quantity" to quantity,
                "price" to price,
                "lastUpdated" to System.currentTimeMillis(),
                "available" to (quantity > 0)
            )

            val existingStock = firestore.collection(STOCK_COLLECTION)
                .whereEqualTo("medicineId", medicineId)
                .whereEqualTo("pharmacyId", pharmacyId)
                .limit(1)
                .get()
                .await()

            if (existingStock.documents.isNotEmpty()) {
                firestore.collection(STOCK_COLLECTION)
                    .document(existingStock.documents.first().id)
                    .update(stockData as Map<String, Any>)
                    .await()
            } else {
                firestore.collection(STOCK_COLLECTION)
                    .add(stockData)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock", e)
            Result.failure(e)
        }
    }
}