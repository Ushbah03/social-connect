package com.example.socialconnect1

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PostAdapter(
    private val postList: List<Post>,
    private val onPostClick: (Post) -> Unit = {},
    private val avatarurl: String? = null,
    private val username: String? = null
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val client = OkHttpClient()
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"
    private val supabaseBaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImg: ImageView = itemView.findViewById(R.id.profileImageView)
        val username: TextView = itemView.findViewById(R.id.usernameTextView)
        val caption: TextView = itemView.findViewById(R.id.caption)
        val postImg: ImageView = itemView.findViewById(R.id.postImg)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val likeBtn: ImageView = itemView.findViewById(R.id.likeButton)
        val commentBtn: ImageView = itemView.findViewById(R.id.commentButton)
        val shareBtn: ImageView = itemView.findViewById(R.id.shareButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.caption.text = post.content
        holder.timestamp.text = formatTimestamp(post.created_at)
        holder.username.text = post.username ?: "Anonymous"

        Glide.with(holder.itemView.context)
            .load(post.avatarurl ?: "")
            .placeholder(R.drawable.profile)
            .into(holder.profileImg)

        if (!post.image_url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context).load(post.image_url).into(holder.postImg)
        } else {
            holder.postImg.setImageResource(R.drawable.post_placeholder)
        }

        // 🔗 Click to open post detail
        holder.postImg.setOnClickListener { onPostClick(post) }

        // 🧑‍💼 Profile navigation
        val context = holder.itemView.context
        val openUserProfile = {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("USER_ID", post.user_id)
            context.startActivity(intent)
        }
        holder.username.setOnClickListener { openUserProfile() }
        holder.profileImg.setOnClickListener { openUserProfile() }



        // 💬 Comment

        holder.commentBtn.setOnClickListener {
            val fragment = CommentBottomSheet(post.id)
            fragment.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "CommentSheet")
        }


        // ❤️ Like
        holder.likeBtn.setOnClickListener {
            animateButton(holder.likeBtn) // 🎉 NEW animation
            val userId = SessionManager.currentUserId ?: return@setOnClickListener
            insertLike(post.id, userId,
                onSuccess = { showToast(holder, "Liked!") },
                onError = { showToast(holder, it) }
            )
        }


// 📤 Share
        holder.shareBtn.setOnClickListener {
            animateShareButton(holder.shareBtn) // Animation
            showToast(holder, "Share clicked")
        }

    }

    override fun getItemCount(): Int = postList.size

    private fun formatTimestamp(raw: String): String {
        return raw.substringBefore("T")
    }

    private fun showToast(holder: PostViewHolder, message: String) {
        holder.itemView.post {
            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCommentDialog(holder: PostViewHolder, postId: String, userId: String) {
        val context = holder.itemView.context
        val editText = EditText(context)
        editText.hint = "Enter your comment"

        AlertDialog.Builder(context)
            .setTitle("Add Comment")
            .setView(editText)
            .setPositiveButton("Post") { _, _ ->
                val commentText = editText.text.toString()
                if (commentText.isNotBlank()) {
                    handleCommentFlow(holder, postId, commentText)
                } else {
                    showToast(holder, "Comment can't be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun insertLike(postId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val accessToken = SessionManager.accessToken ?: ""
        val request = Request.Builder()
            .url("$supabaseBaseUrl/rest/v1/posts?select=user_id&id=eq.$postId")
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Failed to fetch post owner: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return onError("Empty response")
                if (!response.isSuccessful) return onError("Failed to fetch post owner")

                try {
                    val jsonArray = JSONArray(body)
                    val postOwnerId = jsonArray.getJSONObject(0).getString("user_id")

                    val likeJson = JSONObject().apply {
                        put("post_id", postId)
                        put("user_id", userId)
                    }

                    val likeRequest = Request.Builder()
                        .url("$supabaseBaseUrl/rest/v1/likes")
                        .post(likeJson.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("apikey", supabaseKey)
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()

                    client.newCall(likeRequest).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            onError("Failed to like: ${e.message}")
                        }

                        override fun onResponse(call: Call, likeResponse: Response) {
                            if (likeResponse.isSuccessful) {
                                insertNotification(postOwnerId, userId, postId, "like")
                                onSuccess()
                            } else {
                                onError("Failed to like: ${likeResponse.body?.string()}")
                            }
                        }
                    })
                } catch (e: Exception) {
                    onError("JSON parsing error: ${e.message}")
                }
            }
        })
    }

    fun handleCommentFlow(holder: PostViewHolder, postId: String, commentText: String) {
        val currentUserId = SessionManager.currentUserId ?: return

        fetchPostOwner(postId,
            onResult = { postOwnerId ->
                insertComment(postId, currentUserId, commentText,
                    onSuccess = {
                        insertNotification(postOwnerId, currentUserId, postId, "comment")
                        showToast(holder, "Comment posted")
                    },
                    onError = { errorMsg -> showToast(holder, errorMsg) }
                )
            },
            onError = { err -> showToast(holder, err) }
        )
    }

    fun insertComment(postId: String, userId: String, text: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val accessToken = SessionManager.accessToken ?: ""
        val json = JSONObject().apply {
            put("post_id", postId)
            put("user_id", userId)
            put("text", text)
        }

        val request = Request.Builder()
            .url("$supabaseBaseUrl/rest/v1/comments")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Comment insert failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) onSuccess()
                else onError("Comment failed: ${response.body?.string()}")
            }
        })
    }

    fun fetchPostOwner(postId: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val accessToken = SessionManager.accessToken ?: ""
        val request = Request.Builder()
            .url("$supabaseBaseUrl/rest/v1/posts?select=user_id&id=eq.$postId")
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError("Fetch failed: ${e.message}")
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                try {
                    val jsonArray = JSONArray(res)
                    val userId = jsonArray.getJSONObject(0).getString("user_id")
                    onResult(userId)
                } catch (e: Exception) {
                    onError("Parsing error: ${e.message}")
                }
            }
        })
    }

    fun insertNotification(receiverId: String, senderId: String, postId: String, type: String) {
        val accessToken = SessionManager.accessToken ?: ""
        val json = JSONObject().apply {
            put("user_id", receiverId)
            put("sender_id", senderId)
            put("post_id", postId)
            put("type", type)
            put("seen", false)
        }

        val request = Request.Builder()
            .url("$supabaseBaseUrl/rest/v1/notifications")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Notification", "Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Notification", "Response: ${response.code}")
            }
        })
    }
    private fun animateButton(view: ImageView) {
        val context = view.context

        val thumbsUp = ImageView(context).apply {
            setImageResource(R.drawable.thumbs_up) // or your emoji/icon
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                topMargin = view.top
                marginStart = view.left
            }
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
        }

        val parent = view.parent as ViewGroup
        parent.addView(thumbsUp)

        thumbsUp.animate()
            .alpha(1f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(300)
            .withEndAction {
                thumbsUp.animate()
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction {
                        parent.removeView(thumbsUp)
                    }
            }
    }

    private fun animateShareButton(view: View) {
        view.animate()
            .rotationBy(30f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .rotation(0f)
                    .setDuration(100)
            }
    }


}
