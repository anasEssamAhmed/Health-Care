package com.example.healthcare.screen

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcare.R
import com.example.healthcare.screen.doctor.Home
import com.example.healthcare.screen.sick.HomeSick
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class splash : AppCompatActivity() {
    private var auth = Firebase.auth
    private var firestore = Firebase.firestore
    private var gender : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
     //   auth.signOut()
        Handler().postDelayed(
            {
                Log.d("aaa" , auth.currentUser?.email.toString())
                if (auth.currentUser != null){
                    getGender()
                }else {
                    var i = Intent(this, Welcome::class.java)
                    startActivity(i)
                    finish()
                }
            } , 3000)
    }
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
}