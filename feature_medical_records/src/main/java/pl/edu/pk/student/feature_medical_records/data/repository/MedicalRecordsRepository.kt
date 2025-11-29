package pl.edu.pk.student.feature_medical_records.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalRecordsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    private fun getRecordsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("medical_records")

    // Konwersja Uri do Base64 (z kompresją)
    suspend fun convertImageToBase64(imageUri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Kompresja obrazu
            val outputStream = ByteArrayOutputStream()
            val maxSize = 800 // max szerokość/wysokość

            val ratio = Math.min(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height
            )

            val width = (ratio * bitmap.width).toInt()
            val height = (ratio * bitmap.height).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            bitmap.recycle()
            scaledBitmap.recycle()

            Result.success(base64String)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dodanie nowego rekordu
    suspend fun addRecord(record: MedicalRecord): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val recordData = hashMapOf(
                "id" to record.id,
                "type" to record.type.name,
                "title" to record.title,
                "content" to record.content,
                "imageBase64" to record.imageUri, // Teraz to Base64, nie URL
                "timestamp" to record.timestamp,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            getRecordsCollection(userId)
                .document(record.id)
                .set(recordData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Pobieranie rekordów danego typu (real-time updates)
    fun getRecordsByType(type: MedicalRecordType): Flow<List<MedicalRecord>> = callbackFlow {
        val userId = getCurrentUserId()

        val listener = getRecordsCollection(userId)
            .whereEqualTo("type", type.name)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val records = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        MedicalRecord(
                            id = doc.getString("id") ?: "",
                            type = MedicalRecordType.valueOf(doc.getString("type") ?: "TEST_RESULTS"),
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content"),
                            imageUri = doc.getString("imageBase64"), // Base64 string
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(records)
            }

        awaitClose { listener.remove() }
    }

    // Aktualizacja rekordu
    suspend fun updateRecord(record: MedicalRecord): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val updates = hashMapOf<String, Any?>(
                "title" to record.title,
                "content" to record.content,
                "imageBase64" to record.imageUri,
                "timestamp" to record.timestamp
            )

            getRecordsCollection(userId)
                .document(record.id)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Usunięcie rekordu
    suspend fun deleteRecord(recordId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            getRecordsCollection(userId)
                .document(recordId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
