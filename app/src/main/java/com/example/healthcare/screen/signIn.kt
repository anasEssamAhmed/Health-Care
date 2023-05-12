package com.example.healthcare.screen

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcare.databinding.SignInScreenBinding
import com.example.healthcare.screen.doctor.Home
import com.example.healthcare.screen.sick.HomeSick
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class signIn : AppCompatActivity() {
    private lateinit var binding : SignInScreenBinding
    private var auth = Firebase.auth
    private var firestore = Firebase.firestore
    private var gender : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignInScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkConnected()
        binding.signAccountButton.setOnClickListener {
            if (binding.emailTextFieldSignIn.text!!.isNotEmpty() && binding.passwordTextFieldSignIn.text!!.isNotEmpty()) {
                signIn(
                    binding.emailTextFieldSignIn.text.toString(),
                    binding.passwordTextFieldSignIn.text.toString()
                )
                checkConnected()
            }else {
                Toast.makeText(this , "رجاء قم بتعبئة جميع الحقول" , Toast.LENGTH_SHORT).show()
            }
        }

    }
    // sign in
    private fun signIn(email : String , password : String ){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext , "تم تسجيل الدخول بنجاح" , Toast.LENGTH_SHORT).show()
                    getGender()
                    val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
                    // Log the user login event
                    val loginBundle = Bundle()
                    loginBundle.putString(FirebaseAnalytics.Param.METHOD, email)
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, loginBundle)
                }else {
                    Toast.makeText(applicationContext , "يوجد خطا في كلمة المرور او البريد الالكتروني" , Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Know gender form firestore
    private fun getGender(){
        firestore.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                gender = it.getString("gender")
                if (gender == "طبيب"){
                    var i = Intent(this , Home :: class.java)
                    startActivity(i)
                    finishAffinity()
                }else {
                    var i = Intent(this , HomeSick :: class.java)
                    startActivity(i)
                    finishAffinity()
                }
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