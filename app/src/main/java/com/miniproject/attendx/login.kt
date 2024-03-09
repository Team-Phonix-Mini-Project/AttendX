package com.miniproject.attendx

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class login : AppCompatActivity() {
    override fun <T : View?> findViewById(id: Int): T {
        return super.findViewById(id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val buttonView = findViewById<Button>(R.id.button_login)
        buttonView.setOnClickListener {

        }

    }


}

