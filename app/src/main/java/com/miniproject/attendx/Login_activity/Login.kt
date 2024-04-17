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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLoginPage = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindingLoginPage.root)

        // Initialize Firebase Authentication
        fAuth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Check if the user is already logged in
//        if (isLoggedIn()) {
//            navigateToDashboard()
//            return
//        }

        // Set up login button click listener
        submitOnClickAuth()

        // Remember me
        checkedBox()
    }

    private fun checkedBox() {
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        val check = sharedPreferences.getString("name", "")
        if (check.equals("Turu")) {
            navigateToDashboard()
            // About how to store the shared preferences
        }

    }

    private fun submitOnClickAuth() {
        bindingLoginPage.buttonLogin.setOnClickListener {
            if (checkAllFields()) {
                val email = bindingLoginPage.emailInputText.editText?.text.toString()
                val password = bindingLoginPage.passwordInputText.editText?.text.toString()

                // Attempt to sign in with email and password
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        saveLoginStatus()
                        navigateToDashboard()
                    } else {
                        // Display a toast message for login failure
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

    // Check if the user is already logged in
    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    // Save the login status in SharedPreferences
    private fun saveLoginStatus() {
        // Create object of shared preference
        val editor = sharedPreferences.edit()
        editor.putString("name", "Turu")
        editor.apply()
    }

    // Navigate to the dashboard activity
    private fun navigateToDashboard() {
        startActivity(Intent(this, Dashboard_activity::class.java))
        finish()  // Close the current activity to prevent users from going back to the login screen
    }

    // Check if all input fields are filled and valid
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

    // Validate email format
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate password length
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6 // Example: Password must be at least 6 characters long
    }
}
