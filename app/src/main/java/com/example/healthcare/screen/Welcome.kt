package com.example.healthcare.screen

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.example.healthcare.databinding.WelcomeScreenBinding
import java.util.*

class Welcome : AppCompatActivity() {
    private lateinit var binding : WelcomeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkConnected()

        // show app only the arabic language
        val language = Locale.getDefault().language
        if (language == "ar") {

        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("You Can Not use app")
            dialog.setMessage("This application only contains the Arabic language, so if you want to use the application, switch your device to the Arabic language")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Ok") { _, _ ->
                finishAffinity()
            }
            dialog.create().show()
            binding.buttonSignUp.isEnabled = false
            binding.buttonSignIn.isEnabled = false
        }

        binding.buttonSignUp.setOnClickListener {
            val i = Intent(this , signUp :: class.java)
            startActivity(i)
        }
        binding.buttonSignIn.setOnClickListener {
            val i = Intent(this , signIn :: class.java)
            startActivity(i)
        }
    }

    fun checkConnected(){
        // check the internet connected or not
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected) {
            AlertDialog.Builder(this)
                .setTitle("لا يوجد اتصال بالانترنت")
                .setMessage("التطبيق يتطلب منك الاتصال بالانترنت")
                .setPositiveButton("الذهاب الى اعدادات الانترنت") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("الغاء") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}