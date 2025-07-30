package com.example.socialconnect1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class UserProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userBio: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var gridAdapter: GridPostAdapter
    private val postList = mutableListOf<Post>()

    private var profileName: String = ""
    private var profileAvatar: String = ""

    private val supabaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"
    private val accessToken = SessionManager.accessToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        userBio = findViewById(R.id.userBio)
        recyclerView = findViewById(R.id.userPostsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        val userId = intent.getStringExtra("USER_ID")

        Log.d("UserProfileDebug", "Received USER_ID: $userId")

        if (userId != null) {
            fetchUserProfile(userId)
        }
    }

    private fun fetchUserProfile(userId: String) {
        val url = "https://xeuparngwfclppqemmfy.supabase.co/rest/v1/user_profiles?id=eq.$userId&select=name,avatarurl,bio"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    Log.d("UserProfileDebug", "Supabase Response: $it")

                    val userArray = JSONArray(it)
                    if (userArray.length() > 0) {
                        val user = userArray.getJSONObject(0)
                        profileName = user.getString("name")
                        profileAvatar = user.optString("avatarurl", "")

                        runOnUiThread {
                            userName.text = profileName
                            userBio.text = user.optString("bio", "")

                            if (profileAvatar.isNotEmpty()) {
                                Glide.with(this@UserProfileActivity)
                                    .load(profileAvatar)
                                    .into(profileImage)
                            }

                            gridAdapter = GridPostAdapter(
                                posts = postList,
                                onPostClick = { post ->
                                    val intent = Intent(this@UserProfileActivity, PostDetailActivity::class.java).apply {
                                        putExtra("POST_IMAGE", post.image_url)
                                        putExtra("POST_CONTENT", post.content)
                                        putExtra("USERNAME", post.username ?: "Unknown")
                                        putExtra("AVATAR_URL", post.avatarurl ?: "")
                                        putExtra("Time_Posted",post.created_at?:"")
                                    }
                                    startActivity(intent)
                                }
                            )

                            recyclerView.adapter = gridAdapter


                            // Now fetch posts
                            fetchUserPosts(userId)
                        }
                    }
                }
            }
        })
    }

    private fun fetchUserPosts(userId: String) {
        val url = "$supabaseUrl/rest/v1/posts?user_id=eq.$userId&select=*"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val posts = JSONArray(it)
                    postList.clear()
                    for (i in 0 until posts.length()) {
                        val obj = posts.getJSONObject(i)
                        val post = Post(
                            id = obj.getString("id"),
                            user_id = obj.getString("user_id"),
                            content = obj.optString("content", ""),
                            image_url = obj.optString("image_url", ""),
                            created_at = obj.getString("created_at"),
                            username = profileName,
                            avatarurl = profileAvatar
                        )
                        postList.add(post)
                    }
                    runOnUiThread {
                        gridAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
