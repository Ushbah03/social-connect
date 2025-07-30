package com.example.socialconnect1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var captionEditText: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var postButton: Button
    private lateinit var selectImageButton: Button

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 101

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Try to get user_id from intent
        userId = intent.getStringExtra("user_id") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID missing. Cannot create post.", Toast.LENGTH_LONG).show()
            finish() // Exit activity
            return
        }

        captionEditText = findViewById(R.id.captionEditText)
        imagePreview = findViewById(R.id.imagePreview)
        postButton = findViewById(R.id.postButton)
        selectImageButton = findViewById(R.id.selectImageButton)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        postButton.setOnClickListener {
            val caption = captionEditText.text.toString().trim()

            if (caption.isEmpty()) {
                Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val supabaseUrl = "https://xeuparngwfclppqemmfy.supabase.co"
                    val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"
                    val bucketName = "postimages"

                    val imageUrl = selectedImageUri?.let {
                        uploadImageToSupabase(
                            context = this@CreatePostActivity,
                            imageUri = it,
                            bucketName = bucketName,
                            supabaseUrl = supabaseUrl,
                            supabaseKey = supabaseKey,
                            userId = userId
                        )
                    }

                    val success = insertPostToSupabase(
                        supabaseUrl = supabaseUrl,
                        supabaseKey = supabaseKey,
                        userId = userId,
                        content = caption,
                        imageUrl = imageUrl
                    )

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@CreatePostActivity, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@CreatePostActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CreatePostActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }
}
