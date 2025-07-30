package com.example.socialconnect1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.socialconnect1.databinding.FragmentProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var userId: String

    private lateinit var binding: FragmentProfileBinding
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ✅ Get user_id from the intent
        userId = activity?.intent?.getStringExtra("user_id") ?: ""

        // Choose image
        binding.profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save profile
        binding.saveProfileButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val bio = binding.bioEditText.text.toString().trim()

            if (name.isEmpty() || bio.isEmpty() || imageUri == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val uploadedImageUrl = uploadProfileImage(userId, imageUri!!, requireContext())
                    val saved = saveUserProfile(userId, name, bio, uploadedImageUrl)

                    withContext(Dispatchers.Main) {
                        if (saved) {
                            Toast.makeText(requireContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Load profile if already saved
        loadProfile()
    }

    private fun loadProfile() {
        // TODO: Load profile from DB (if already saved)
        // Example: Glide.with(this).load(profileUrl).into(binding.profileImageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.profileImageView)
        }
    }
}
