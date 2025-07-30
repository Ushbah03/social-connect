package com.example.socialconnect1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.socialconnect1.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        SessionManager.currentUserId = prefs.getString("user_id", null)
        // Load default HomeFragment
        loadFragment(HomeFragment())
        // Handle navigation item clicks
        binding.editProfileButton.setOnClickListener {
            val userId = intent.getStringExtra("user_id") ?: return@setOnClickListener
            val token = intent.getStringExtra("access_token") ?: return@setOnClickListener

            val intent = Intent(this, CreatePostActivity::class.java)

            intent.putExtra("user_id", userId)
            intent.putExtra("access_token", token)
            startActivity(intent)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                    true
                }
                R.id.menu_notifications -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NotificationFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    override fun onStart() {
        super.onStart()

        val currentUserId = SessionManager.currentUserId ?: return
        NotificationRealtime.startListening(currentUserId) { message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        NotificationRealtime.stopListening()
    }

}
