package pl.edu.pk.student.feature_medical_records.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pl.edu.pk.student.feature_medical_records.data.remote.SupabaseStorageService
import pl.edu.pk.student.feature_medical_records.data.remote.SupabaseUploadResult
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.set

@Singleton
class MedicalRecordsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext val context: Context,
    val supabaseStorageService: SupabaseStorageService
    ) {
    fun getCurrentUserId(): String {
        val uid = auth.currentUser?.uid
        Log.d("MedicalRecordsRepo", "Getting current user ID: $uid")
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    private fun getRecordsCollection(userId: String, type: MedicalRecordType? = null) =
        if (type == MedicalRecordType.XRAY) {
            firestore.collection("users").document(userId).collection("medicalRecords")
        } else {
            firestore.collection("users").document(userId).collection("medical_records")
        }
    fun convertImageToBase64(imageUri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            val maxSize = 800

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

    suspend fun addRecord(record: MedicalRecord): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val recordData = hashMapOf(
                "id" to record.id,
                "type" to record.type.name,
                "title" to record.title,
                "content" to record.content,
                "imageBase64" to record.imageUri,
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

    fun getRecordsByType(type: MedicalRecordType): Flow<List<MedicalRecord>> = callbackFlow {
        val userId = getCurrentUserId()

        val collectionName = if (type == MedicalRecordType.XRAY) {
            "medicalRecords"
        } else {
            "medical_records"
        }

        val listener = firestore.collection("users")
            .document(userId)
            .collection(collectionName)
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
                            imageUri = doc.getString("imageBase64"),

                            supabaseStoragePath = doc.getString("supabaseStoragePath"),
                            supabasePublicUrl = doc.getString("supabasePublicUrl"),
                            supabaseSignedUrl = doc.getString("supabaseSignedUrl"),
                            supabaseSignedUrlExpiry = doc.getLong("supabaseSignedUrlExpiry"),

                            externalImageUrl = doc.getString("externalImageUrl"),
                            externalImageDeleteUrl = doc.getString("externalImageDeleteUrl"),

                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e("MedicalRecordsRepo", "Error parsing record: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(records)
            }

        awaitClose { listener.remove() }
    }

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


    suspend fun deleteRecord(recordId: String, type: MedicalRecordType): Result<Unit> {
        return try {
            val userId = getCurrentUserId()

            val collectionName = if (type == MedicalRecordType.XRAY) {
                "medicalRecords"
            } else {
                "medical_records"
            }

            firestore.collection("users")
                .document(userId)
                .collection(collectionName)
                .document(recordId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun addRecordWithSupabaseImage(
        type: MedicalRecordType,
        title: String,
        uploadResult: SupabaseUploadResult,
        content: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            val recordId = UUID.randomUUID().toString()

            Log.d("MedicalRecordsRepo", "=== Saving to Firestore (Supabase) ===")
            Log.d("MedicalRecordsRepo", "User ID: $userId")
            Log.d("MedicalRecordsRepo", "Record ID: $recordId")
            Log.d("MedicalRecordsRepo", "Storage path: ${uploadResult.path}")

            if (!uploadResult.path.contains("users/$userId/")) {
                Log.e("MedicalRecordsRepo", "Security violation: path doesn't match userId")
                return@withContext Result.failure(
                    SecurityException("Storage path doesn't match authenticated user")
                )
            }

            val signedUrlExpiry = System.currentTimeMillis() + (3600 * 1000)

            val record = hashMapOf(
                "id" to recordId,
                "type" to type.name,
                "title" to title,
                "content" to content,
                "supabaseStoragePath" to uploadResult.path,
                "supabasePublicUrl" to uploadResult.publicUrl,
                "supabaseSignedUrl" to uploadResult.signedUrl,
                "supabaseSignedUrlExpiry" to signedUrlExpiry,
                "timestamp" to System.currentTimeMillis(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            Log.d("MedicalRecordsRepo", "Record data: $record")

            firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document(recordId)
                .set(record)
                .await()

            Log.d("MedicalRecordsRepo", "Save successful!")

            Result.success(recordId)
        } catch (e: Exception) {
            Log.e("MedicalRecordsRepo", "Save failed: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun deleteRecordWithSupabaseImage(recordId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()

            val doc = firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document(recordId)
                .get()
                .await()

            val storagePath = doc.getString("supabaseStoragePath")

            if (storagePath != null) {
                supabaseStorageService.deleteFile(storagePath)
            }

            firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document(recordId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun refreshSignedUrl(recordId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()

            val doc = firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document(recordId)
                .get()
                .await()

            val storagePath = doc.getString("supabaseStoragePath")
                ?: return@withContext Result.failure(Exception("No storage path found"))

            val signedUrlResult = supabaseStorageService.generateSignedUrl(
                path = storagePath,
                expiresInHours  = 24
            )

            signedUrlResult.fold(
                onSuccess = { newSignedUrl ->
                    val updates = hashMapOf<String, Any>(
                        "supabaseSignedUrl" to newSignedUrl,
                        "supabaseSignedUrlExpiry" to (System.currentTimeMillis() + 86400000)
                    )

                    firestore.collection("users")
                        .document(userId)
                        .collection("medicalRecords")
                        .document(recordId)
                        .update(updates)
                        .await()

                    Result.success(newSignedUrl)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

