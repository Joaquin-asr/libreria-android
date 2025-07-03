package com.libreria.androidproject.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.libreria.androidproject.R
import com.libreria.androidproject.repository.FirestoreUserRepository
import com.libreria.androidproject.ui.activity.AdminDashboardActivity
import com.libreria.androidproject.ui.activity.UserDashboardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val authManager = FirebaseAuthManager
    private val userRepo = FirestoreUserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUser = findViewById<TextInputEditText>(R.id.etUsername)
        val etPass = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoRegister = findViewById<MaterialButton>(R.id.btnGoToRegister)


        btnLogin.setOnClickListener {
            val email = etUser.text.toString().trim()
            val pass  = etPass.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val user = authManager
                        .signIn(email, pass)
                        ?: throw Exception("Credenciales inválidas")

                    val profile = userRepo.getUser(user.uid)
                        ?: throw Exception("Usuario no encontrado en Firestore")

                    when (profile.role) {
                        "Administrador" -> startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        "Usuario"       -> startActivity(Intent(this@LoginActivity, UserDashboardActivity::class.java))
                        else            -> throw Exception("Rol desconocido")
                    }
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LoginError", "Falló el login", e)
                    Toast.makeText(this@LoginActivity, "Error: ${e.localizedMessage ?: e.toString()}", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}