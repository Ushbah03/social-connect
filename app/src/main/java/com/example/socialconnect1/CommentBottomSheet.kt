package com.example.socialconnect1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CommentBottomSheet(private val postId: String) : BottomSheetDialogFragment() {

    private val client = OkHttpClient()
    private val supabaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"

    private lateinit var commentListLayout: LinearLayout
    private lateinit var addCommentEditText: EditText
    private lateinit var postButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_comments, container, false)
        commentListLayout = view.findViewById(R.id.commentListLayout)
        addCommentEditText = view.findViewById(R.id.commentEditText)
        postButton = view.findViewById(R.id.postCommentButton)

        fetchComments()

        postButton.setOnClickListener {
            val text = addCommentEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                insertComment(text)
            }
        }

        return view
    }

    private fun fetchComments() {
        val accessToken = SessionManager.accessToken ?: return
        val url = "$supabaseUrl/rest/v1/comments?post_id=eq.$postId&select=*,user_profiles(name,avatarurl)"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonArray = JSONArray(it)
                    activity?.runOnUiThread {
                        commentListLayout.removeAllViews()
                        for (i in 0 until jsonArray.length()) {
                            val comment = jsonArray.getJSONObject(i)
                            val text = comment.getString("text")
                            val user = comment.getJSONObject("user_profiles")
                            val username = user.optString("name", "")
                            val avatarUrl = user.optString("avatarurl", "")

                            val commentView = layoutInflater.inflate(R.layout.item_comment, commentListLayout, false)
                            val avatar = commentView.findViewById<ImageView>(R.id.commentUserAvatar)
                            val name = commentView.findViewById<TextView>(R.id.commentUsername)
                            val content = commentView.findViewById<TextView>(R.id.commentText)

                            name.text = username
                            content.text = text

                            Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.profile)
                                .circleCrop()
                                .into(avatar)

                            commentListLayout.addView(commentView)
                        }
                    }
                }
            }
        })
    }

    private fun insertComment(commentText: String) {
        val userId = SessionManager.currentUserId ?: return
        val accessToken = SessionManager.accessToken ?: return

        val json = JSONObject().apply {
            put("post_id", postId)
            put("user_id", userId)
            put("text", commentText)
        }

        val request = Request.Builder()
            .url("$supabaseUrl/rest/v1/comments")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        addCommentEditText.setText("")
                        fetchComments()
                    }
                }
            }
        })
    }
}
