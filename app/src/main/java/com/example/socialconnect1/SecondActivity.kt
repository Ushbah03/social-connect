package com.example.socialconnect1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect1.databinding.ActivitySecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val res = login(email, password)
                        val userId = res.user.id
                        SessionManager.accessToken = res.access_token

                        // ✅ Save userId in SessionManager
                        SessionManager.currentUserId = userId

                        // ✅ Save in SharedPreferences
                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("user_id", userId)
                            .putString("access_token", res.access_token)
                            .apply()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SecondActivity, "Login successful", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@SecondActivity, HomeActivity::class.java)
                            intent.putExtra("user_id", userId)
                            intent.putExtra("access_token", res.access_token)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SecondActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgetPassword::class.java))
        }
    }
}
