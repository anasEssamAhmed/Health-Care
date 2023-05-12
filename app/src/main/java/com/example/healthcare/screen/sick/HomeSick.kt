package com.example.healthcare.screen.sick

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.healthcare.R
import com.example.healthcare.databinding.HomeScreenBinding
import com.example.healthcare.funcation.Massage
import com.example.healthcare.funcation.firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeSick : AppCompatActivity() {
    private lateinit var binding: HomeScreenBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenBinding.inflate(layoutInflater)
        auth = Firebase.auth
        firestore = Firebase.firestore
        setContentView(binding.root)
        checkConnected()
        // The name of the disease is scrolled until a topic of its own is created
        firebase().convert {
            Massage().subscribeTopic(it)
        }

        // get Token and store in the firestore
        Massage().getToken {
            val uid = auth.currentUser!!.uid
            val map = mapOf(
                "token" to it
            )
            firestore.collection("Token").document(uid)
                .set(map)
                .addOnSuccessListener {
                    Log.d("aaa" , "add token to firestore")
                }
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.myAccount -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContiner.id, Profile())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.post -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContiner.id, ArticlesFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.doctorChat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContiner.id, AvailableDoctors())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }

        }
        binding.bottomNavigationView.selectedItemId = R.id.post
    }

    override fun onBackPressed() {
        firebase().exitTheApp(this)
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