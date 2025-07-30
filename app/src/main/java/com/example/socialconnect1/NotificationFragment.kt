package com.example.socialconnect1

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()
    val json = Json { ignoreUnknownKeys = true }

    private val supabaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        recyclerView = view.findViewById(R.id.rc_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchNotifications()
        return view
    }

    private fun fetchNotifications() {
        val userId = SessionManager.currentUserId
        val accessToken = SessionManager.accessToken

        if (userId.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            Log.e("NotifDebug", "❗ userId or accessToken is null")
            return
        }

        val url = "$supabaseUrl/rest/v1/notifications?user_id=eq.$userId&select=*"
        Log.d("NotifDebug", "📤 Fetching notifications from: $url")
        Log.d("NotifDebug", "🔐 Access Token: $accessToken")

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotifDebug", "🚫 Network Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("NotifDebug", "📦 Response JSON = $responseData")

                if (!response.isSuccessful || responseData == null) {
                    Log.e("NotifDebug", "❌ Failed to fetch notifications.")
                    return
                }

                val jsonArray = JSONArray(responseData)
                if (jsonArray.length() == 0) {
                    Log.d("NotifDebug", "⚠️ No notifications found for user: $userId")
                    requireActivity().runOnUiThread {
                        recyclerView.adapter = NotificationAdapter(emptyList())
                    }
                    return
                }

                val notificationList = mutableListOf<Notification>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val id = json.getString("id")
                    val user_id = json.getString("user_id")
                    val sender_id = json.getString("sender_id")
                    val post_id = json.optString("post_id", "")
                    val type = json.getString("type")
                    val seen = json.optBoolean("seen", false)
                    val created_at = json.getString("created_at")

                    val notification = Notification(
                        id = id,
                        user_id = user_id,
                        sender_id = sender_id,
                        post_id = post_id,
                        type = type,
                        seen = seen,
                        created_at = created_at
                    )
                    notificationList.add(notification)
                }

                fetchSenderDetails(notificationList)
            }
        })
    }
    private fun fetchSenderDetails(notifications: List<Notification>) {
        val accessToken = SessionManager.accessToken

        if (accessToken.isNullOrEmpty()) {
            Log.e("NotifDebug", "❗ AccessToken missing during sender fetch")
            return
        }

        val userIds = notifications.map { it.sender_id }.toSet()
        if (userIds.isEmpty()) {
            Log.e("NotifDebug", "❗ No sender IDs found")
            return
        }
        val userQuery = userIds.joinToString(",")  // no single quotes needed


        val url = "$supabaseUrl/rest/v1/user_profiles?id=in.($userQuery)&select=id,name,avatarurl"


        Log.d("NotifDebug", "📤 Fetching sender profiles from: $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotifDebug", "🚫 Failed to fetch sender profiles: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("NotifDebug", "🧾 Raw Sender JSON = $body")

                if (!response.isSuccessful || body == null) {
                    Log.e("NotifDebug", "❌ Failed to parse sender profile response")
                    return
                }

                try {
                    val jsonArray = JSONArray(body)
                    Log.d("NotifDebug", "✅ Sender JSON Array length: ${jsonArray.length()}")
                    val senderMap = mutableMapOf<String, Pair<String, String>>()

                    for (i in 0 until jsonArray.length()) {
                        val user = jsonArray.getJSONObject(i)
                        val id = user.getString("id")  // ✅ or "user_id" if your table has that instead
                        val username = user.optString("name", "Unknown")  // ✅ this fixes it
                        val avatar = user.optString("avatarurl", "")
                        senderMap[id] = Pair(username, avatar)
                    }



                    val enriched = notifications.map { notif ->
                        val (name, avatar) = senderMap[notif.sender_id] ?: Pair("Unknown", "")
                        notif.senderName = name
                        notif.senderAvatar = avatar
                        notif
                    }

                    requireActivity().runOnUiThread {
                        recyclerView.adapter = NotificationAdapter(enriched)
                    }

                } catch (e: Exception) {
                    Log.e("NotifDebug", "❌ JSON Parsing failed: ${e.message}")
                }
            }
        })
    }


}
