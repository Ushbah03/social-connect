package com.example.socialconnect1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect1.databinding.ActivityResetPasswordBinding
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resetPasswordButton.setOnClickListener {
            val oobCode = binding.codeEditText.text.toString().trim()
            val newPassword = binding.passwordEditText.text.toString().trim()

            if (oobCode.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter code and new password", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = resetPasswordWithCode(oobCode, newPassword)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@ResetPasswordActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ResetPasswordActivity, SecondActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ResetPasswordActivity, "Failed to update password", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun resetPasswordWithCode(oobCode: String, newPassword: String): Boolean {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response: HttpResponse = client.post("https://xeuparngwfclppqemmfy.supabase.co/auth/v1/verify") {
            contentType(ContentType.Application.Json)
            header(
                "apikey",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM"
            )
            setBody(
                """{
                    "type": "recovery",
                    "token": "$oobCode",
                    "password": "$newPassword"
                }"""
            )
        }

        return response.status == HttpStatusCode.OK
    }
}
