package com.example.socialconnect1

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import com.bumptech.glide.Glide
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PostDetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var usernameView: TextView
    private lateinit var contentView: TextView
    private lateinit var avatarView: ImageView
    private lateinit var time: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        imageView = findViewById(R.id.postImage)
        usernameView = findViewById(R.id.UN)
        contentView = findViewById(R.id.postContent)
        avatarView = findViewById(R.id.userAvatar)
        time = findViewById(R.id.created_at)

        val imageUrl = intent.getStringExtra("POST_IMAGE")
        val content = intent.getStringExtra("POST_CONTENT")
        val username = intent.getStringExtra("USERNAME")
        val avatarUrl = intent.getStringExtra("AVATAR_URL")
        val rawTimestamp = intent.getStringExtra("Time_Posted")
        val parsedDate = rawTimestamp?.let {
            OffsetDateTime.parse(it).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        time.text = parsedDate
        usernameView.text = username
        contentView.text = content
        time.text = parsedDate  // ✅ correctly set

        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(avatarView)
        }

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }
    }

}
