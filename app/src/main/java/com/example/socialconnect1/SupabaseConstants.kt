package com.example.socialconnect1

import android.content.Context
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.InputStream

import java.util.UUID
import okhttp3.MediaType.Companion.toMediaTypeOrNull

object SupabaseConstants {
    const val SUPABASE_URL = "https://xeuparngwfclppqemmfy.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"
    const val BUCKET_NAME = "postimages"
    const val POSTS_TABLE = "posts"
}


fun uploadImageToSupabase(
    context: Context,
    imageUri: Uri,
    bucketName: String,
    supabaseUrl: String,
    supabaseKey: String,
    userId: String
): String? {
    val fileName = "post_${UUID.randomUUID()}.jpg"
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val bytes = inputStream?.readBytes() ?: return null

    val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
    val request = Request.Builder()
        .url("$supabaseUrl/storage/v1/object/$bucketName/$userId/$fileName")
        .addHeader("Authorization", "Bearer $supabaseKey")
        .addHeader("x-upsert", "true")
        .put(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
        println("Image Upload → Code: ${response.code}, Message: ${response.message}")
        val body = response.body?.string()
        println("Upload Response Body: $body")

        if (response.isSuccessful) {
            return "$supabaseUrl/storage/v1/object/public/$bucketName/$userId/$fileName"
        }
    }


    return null
}
fun insertPostToSupabase(
    supabaseUrl: String,
    supabaseKey: String,
    userId: String,
    content: String,
    imageUrl: String?
): Boolean {
    val json = """
        {
            "user_id": "$userId",
            "content": "$content",
            "image_url": ${if (imageUrl != null) "\"$imageUrl\"" else null}
        }
    """.trimIndent()

    val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

    val request = Request.Builder()
        .url("$supabaseUrl/rest/v1/posts")
        .addHeader("Authorization", "Bearer $supabaseKey")
        .addHeader("apikey", supabaseKey)
        .addHeader("Content-Type", "application/json")
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
        val body = response.body?.string()
        println("Post Insert → Code: ${response.code}, Message: ${response.message}")
        println("Insert Response Body: $body")

        return response.isSuccessful
    }

}
