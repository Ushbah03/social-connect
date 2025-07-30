package com.example.socialconnect1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialconnect1.databinding.ActivityManualResetPasswordBinding
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class ManualResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManualResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resetPasswordButton.setOnClickListener {
            val oobCode = binding.codeEditText.text.toString().trim()
            val newPassword = binding.passwordEditText.text.toString().trim()

            if (oobCode.isNotEmpty() && newPassword.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = resetPasswordWithCode(oobCode, newPassword)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@ManualResetPasswordActivity, "Password updated", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ManualResetPasswordActivity, "Failed to reset password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

suspend fun resetPasswordWithCode(oobCode: String, newPassword: String): Boolean {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    val response = client.post("https://xeuparngwfclppqemmfy.supabase.co/auth/v1/verify") {
        contentType(ContentType.Application.Json)
        header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM")
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