package pl.edu.pk.student.feature_medical_records.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {

    private const val SUPABASE_URL = ""
    private const val SUPABASE_KEY = ""
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
    }

    val storage = client.storage
}