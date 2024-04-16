package com.miniproject.attendx.Login_activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.miniproject.attendx.Dashboard.Dashboard_activity
import com.miniproject.attendx.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var bindingLoginPage: ActivityLoginBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
//    private var flag : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLoginPage = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindingLoginPage.root)

        fAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Check if the user is already logged in
//        flag = intent.getBooleanExtra("fuckOFF", false)
        if (isLoggedIn()) {
            navigateToDashboard()
            return
        }

        bindingLoginPage.buttonLogin.setOnClickListener {
            if (checkAllFields()) {
                val email = bindingLoginPage.emailInputText.editText?.text.toString()
                val password = bindingLoginPage.passwordInputText.editText?.text.toString()

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        saveLoginStatus(true)
                        navigateToDashboard()
//                        flag = true
                    } else {
                        // Display a single toast message for login failure
                        Toast.makeText(
                            applicationContext,
                            "Failed to sign in. Please check your credentials and try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, Dashboard_activity::class.java))
        finish()  // Close the current activity to prevent users from going back to the login screen
    }

    private fun checkAllFields(): Boolean {
        val email = bindingLoginPage.emailInputText.editText?.text.toString()
        val password = bindingLoginPage.passwordInputText.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isEmailValid(email)) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6 // Example: Password must be at least 6 characters long
    }
}
