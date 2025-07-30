package com.example.socialconnect1

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

fun createSupabaseClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }
}

val supabaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"
val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"

suspend fun uploadProfileImage(userId: String, avatarUri: Uri, context: Context): String? {
    val inputStream = context.contentResolver.openInputStream(avatarUri) ?: return null
    val bytes = inputStream.readBytes()
    val fileName = "profile_$userId.jpg"

    val response = createSupabaseClient().post("$supabaseUrl/storage/v1/object/avatarurl/$fileName") {
        header("Authorization", "Bearer $supabaseKey")
        header("apikey", supabaseKey)
        contentType(ContentType.Image.JPEG)
        setBody(bytes)
    }

    println("Upload Status: ${response.status.value}")
    println("Upload Body: ${response.bodyAsText()}")

    return if (response.status.value in 200..299) {
        "$supabaseUrl/storage/v1/object/public/avatarurl/$fileName"
    } else null
}

suspend fun saveUserProfile(userId: String, name: String, bio: String, avatarurl: String?): Boolean {
    val body = buildJsonObject {
        put("id", userId)
        put("name", name)
        put("bio", bio)
        if (avatarurl != null) put("avatarurl", avatarurl) // fixed spelling too
    }
    val response = createSupabaseClient().post("$supabaseUrl/rest/v1/user_profiles") {
        header("apikey", supabaseKey)
        header("Authorization", "Bearer $supabaseKey")
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    println("Save Profile Status: ${response.status.value}")
    println("Save Profile Body: ${response.bodyAsText()}")

    return response.status.value in 200..299
}
suspend fun fetchPostsFromSupabase(accessToken: String): List<Post> {
    val client = OkHttpClient()
    val url = "${SupabaseConstants.SUPABASE_URL}/rest/v1/posts?select=*"
    val request = Request.Builder()
        .url(url)
        .get()
        .addHeader("apikey", SupabaseConstants.SUPABASE_KEY)
        .addHeader("Authorization", "Bearer $accessToken")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    if (response.isSuccessful && responseBody != null) {
        val gson = Gson()
        return gson.fromJson(responseBody, Array<Post>::class.java).toList()
    } else {
        throw Exception("Failed to fetch posts: ${response.code} ${response.message}")
    }
}
data class Post(

    val id: String,
    val user_id: String,
    val content: String,
    val image_url: String?,
    val created_at: String,
    val username: String?,            // optional
    val avatarurl: String?,        //optional
    )
fun insertLike(postId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val url = "https://xeuparngwfclppqemmfy.supabase.co/rest/v1/likes"
    val client = OkHttpClient()

    val bodyJson = JSONObject()
    bodyJson.put("post_id", postId)
    bodyJson.put("user_id", userId)

    val request = Request.Builder()
        .url(url)
        .post(RequestBody.create("application/json".toMediaType(), bodyJson.toString()))
        .addHeader("apikey", supabaseKey)
        .addHeader("Authorization", "Bearer $supabaseKey")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Like insert failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Like insert failed: ${response.body?.string()}")
            }
        }
    })
}

fun insertComment(postId: String, userId: String, text: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val url = "https://xeuparngwfclppqemmfy.supabase.co/rest/v1/comments"
    val client = OkHttpClient()

    val bodyJson = JSONObject()
    bodyJson.put("post_id", postId)
    bodyJson.put("user_id", userId)
    bodyJson.put("text", text)

    val request = Request.Builder()
        .url(url)
        .post(RequestBody.create("application/json".toMediaType(), bodyJson.toString()))
        .addHeader("apikey", supabaseKey)
        .addHeader("Authorization", "Bearer $supabaseKey")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Comment insert failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Comment insert failed: ${response.body?.string()}")
            }
        }
    })
}
