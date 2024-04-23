package com.miniproject.attendx.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.Login_activity.LoginActivity
import com.miniproject.attendx.R

class SplashScreenActiivty : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000 // 2 seconds delay
    private lateinit var logoImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        val logoImageView = findViewById<ImageView>(R.id.logoImageView)

        // Load the fade-in animation
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_from_bottom_to_top)

        // Apply the animation to the logo
        logoImageView.startAnimation(fadeInAnimation)


        // Navigate to the main activity after the animation completes
        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                val mainActivityIntent =
                    Intent(this@SplashScreenActiivty, LoginActivity::class.java)
                startActivity(mainActivityIntent)
                finish() // Finish the splash screen activity
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}