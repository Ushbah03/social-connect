package com.example.socialconnect1
//Users.kt
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: User
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String? = null
)



@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val bio: String? = null,
    val avatarurl: String? = null

)

suspend fun getUserProfile(userId: String): UserProfile? {
    val response = createSupabaseClient().get("$supabaseUrl/rest/v1/user_profiles") {
        header("apikey", supabaseKey)
        header("Authorization", "Bearer $supabaseKey")
        parameter("id", "eq.$userId")
        accept(ContentType.Application.Json)
    }

    val profiles: List<UserProfile> = response.body()
    return profiles.firstOrNull()
}
