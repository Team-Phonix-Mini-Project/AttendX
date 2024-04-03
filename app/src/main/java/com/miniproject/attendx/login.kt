package com.miniproject.attendx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.miniproject.attendx.Dashboard.Dashboard_activity
import com.miniproject.attendx.databinding.ActivityLoginBinding


class login : AppCompatActivity() {
    lateinit var bindingLoginPage:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingLoginPage=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindingLoginPage.root)
        bindingLoginPage.buttonLogin.setOnClickListener {
            val intent=Intent(this, Dashboard_activity::class.java)
            startActivity(intent)

            val Intent = Intent(this,Dashboard_activity::class.java)

            startActivity(Intent)

        }


    }


}

