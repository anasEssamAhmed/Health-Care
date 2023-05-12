package com.example.healthcare.screen

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcare.R


class splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        Handler().postDelayed(
            {
                var  i = Intent(this , Welcome :: class.java)
                startActivity(i)
                finish()
            } , 3000)
    }
}