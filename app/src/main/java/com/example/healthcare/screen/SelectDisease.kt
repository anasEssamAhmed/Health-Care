package com.example.healthcare.screen

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.healthcare.R
import com.example.healthcare.databinding.SelectDiseaseBinding
import com.example.healthcare.screen.sick.HomeSick
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SelectDisease : AppCompatActivity() {
    private lateinit var binding: SelectDiseaseBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkConnected()
        auth = Firebase.auth
        binding.saveData.setOnClickListener {
            if (intent.getBooleanExtra("isTrueOrNot", false)) {
                updateDisease()
            } else {
                addToFirestore()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val data = resources.getStringArray(R.array.arrayOfDisease)
        val arrayAdapter = ArrayAdapter(this, R.layout.drop_down_menu, data)
        binding.selectDiseaseInput.setAdapter(arrayAdapter)
    }

    // add the Disease to Firestore
    private fun addToFirestore() {
        val ref = firestore.collection("sick Information").document(auth.currentUser!!.uid)
        Log.d("ooo", "this is data of spinner : ${binding.selectDiseaseInput.text}")
        val data = hashMapOf(
            "المرض" to binding.selectDiseaseInput.text.toString()
        )
        ref.set(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "تمت الحفظ بنجاح", Toast.LENGTH_SHORT).show()
                val i = Intent(applicationContext, signIn::class.java)
                startActivity(i)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    "يوجد مشكلة في الحفظ , قم بالمحاولة مرة اخرى",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun updateDisease() {
        val ref = firestore.collection("sick Information").document(auth.currentUser!!.uid)
        ref.update("المرض", binding.selectDiseaseInput.text.toString())
            .addOnSuccessListener {
                Toast.makeText(applicationContext , "تم التحديث بنجاح"  , Toast.LENGTH_SHORT).show()
                val i = Intent(this , HomeSick :: class.java)
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