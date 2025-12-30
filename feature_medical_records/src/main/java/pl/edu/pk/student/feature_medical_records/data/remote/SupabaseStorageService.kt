package pl.edu.pk.student.feature_medical_records.data.remote

import android.util.Log
import io.github.jan.supabase.storage.BucketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

data class SupabaseUploadResult(
    val fileName: String,
    val path: String,
    val publicUrl: String,
    val signedUrl: String,
    val size: Long
)

@Singleton
class SupabaseStorageService @Inject constructor() {

    companion object {
        private const val TAG = "SupabaseStorage"
        private const val BUCKET_NAME = "medical-images"
        private const val MAX_FILE_SIZE = 100 * 1024 * 1024 // 100 MB
    }

    private val bucket: BucketApi
        get() = SupabaseClient.storage.from(BUCKET_NAME)


    suspend fun uploadDicomFile(
        file: File,
        userId: String,
        fileName: String? = null
    ): Result<SupabaseUploadResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Supabase Upload Started ===")
            Log.d(TAG, "File: ${file.name}, size: ${file.length()} bytes")
            Log.d(TAG, "User ID: $userId")

            if (!file.exists()) {
                return@withContext Result.failure(
                    IllegalArgumentException("File does not exist: ${file.path}")
                )
            }

            if (file.length() > MAX_FILE_SIZE) {
                val sizeMB = file.length() / (1024.0 * 1024.0)
                return@withContext Result.failure(
                    IllegalArgumentException(
                        "File size (%.2f MB) exceeds 100 MB limit".format(sizeMB)
                    )
                )
            }

            val fileExtension = file.extension.ifEmpty { "dcm" }
            val uniqueFileName = fileName?.let {
                "${it.replace("[^a-zA-Z0-9.-]".toRegex(), "_")}_${UUID.randomUUID()}.$fileExtension"
            } ?: "${UUID.randomUUID()}.$fileExtension"

            val storagePath = "users/$userId/xrays/$uniqueFileName"

            Log.d(TAG, "Uploading to path: $storagePath")

            val fileBytes = file.readBytes()

            bucket.upload(
                path = storagePath,
                data = fileBytes,
                upsert = false
            )

            Log.d(TAG, "File uploaded successfully")

            val publicUrl = bucket.publicUrl(storagePath)

            val signedUrl = bucket.createSignedUrl(
                path = storagePath,
                expiresIn = 1.hours
            )

            Log.d(TAG, "Public URL: $publicUrl")
            Log.d(TAG, "Signed URL: $signedUrl")

            val result = SupabaseUploadResult(
                fileName = uniqueFileName,
                path = storagePath,
                publicUrl = publicUrl,
                signedUrl = signedUrl,
                size = file.length()
            )

            Log.d(TAG, "Upload successful!")
            Result.success(result)

        } catch (e: Exception) {
            Log.e(TAG, "Upload error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting file: $path")

            bucket.delete(path)

            Log.d(TAG, "File deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete error: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun generateSignedUrl(
        path: String,
        expiresInHours: Int = 24
    ): Result<String> = withContext(Dispatchers.IO) {
        try {

            val signedUrl = bucket.createSignedUrl(
                path = path,
                expiresIn = expiresInHours.hours
            )

            Log.d(TAG, "Signed URL generated: $signedUrl")
            Result.success(signedUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Signed URL error: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun downloadFile(path: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading file: $path")

            val fileBytes = bucket.downloadAuthenticated(path)

            Log.d(TAG, "File downloaded: ${fileBytes.size} bytes")
            Result.success(fileBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            Result.failure(e)
        }
    }
}