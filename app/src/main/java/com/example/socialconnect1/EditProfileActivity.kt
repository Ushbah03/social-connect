package com.example.socialconnect1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 101

    private var userId: String = ""
    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        bioEditText = findViewById(R.id.bioEditText)
        saveButton = findViewById(R.id.saveProfileButton)

        // ✅ Step 3: Get from Intent instead of Supabase SDK
        userId = intent.getStringExtra("user_id") ?: ""
        accessToken = intent.getStringExtra("access_token") ?: ""

        if (userId.isEmpty() || accessToken.isEmpty()) {
            Toast.makeText(this, "Missing user credentials", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchProfileData()

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val imageUrl = selectedImageUri?.let {
                        uploadProfileImage(userId, it, this@EditProfileActivity)
                    }

                    val success = saveUserProfile(userId, name, bio, imageUrl)

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@EditProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()

                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun fetchProfileData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = getUserProfile(userId)
                withContext(Dispatchers.Main) {
                    nameEditText.setText(profile?.name ?: "")
                    bioEditText.setText(profile?.bio ?: "")
                    // You can also load image with Glide here if profile.avatarurl is not null
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
