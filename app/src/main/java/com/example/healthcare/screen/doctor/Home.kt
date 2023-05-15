package com.example.healthcare.screen.doctor

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import com.example.healthcare.R
import com.example.healthcare.databinding.ActivityHomeBinding
import com.example.healthcare.funcation.Massage
import com.example.healthcare.funcation.firebase
import com.example.healthcare.screen.Welcome
import com.example.healthcare.screen.sick.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auto: FirebaseAuth
    private var firestore =  Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // throw crash for test
   //     throw RuntimeException("Test Crash")
        checkConnected()
        // get Token and save in the firestore
        Massage().getToken {
            val uid = auto.currentUser!!.uid
            val map = mapOf(
                "token" to it
            )
            firestore.collection("Token").document(uid)
                .set(map)
                .addOnSuccessListener {
                    Log.d("aaa" , "add token to firestore")
                }
        }
        // show the action Bar
        setSupportActionBar(binding.toolbar)

        // create the drawer layout
        val drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, 0, 0)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // click in the toolbar to open drawer
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Access to the email and name
        auto = Firebase.auth
        var layout = binding.navigationView.getHeaderView(0)
        val name = layout.findViewById<TextView>(R.id.doctorName)
        val email = layout.findViewById<TextView>(R.id.doctorEmail)
        val image = layout.findViewById<ImageView>(R.id.imageProfile)
        val myTypeface = ResourcesCompat.getFont(this, R.font.font)
        if (auto.currentUser != null) {
            firestore.collection("users").document(auto.currentUser!!.uid)
                .get().addOnSuccessListener {
                    val names = it.getString("name")
                    val photo = it.getString("profileImage")
                    name.text = names
                    Picasso.get().load(photo).placeholder(R.drawable.upload).into(image)

                }
            email.text = auto.currentUser!!.email.toString()
            name.typeface = myTypeface
            email.typeface = myTypeface
        }

        // Show fragment
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.myAccountDoctor -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.continer.id, Profile()).commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.postView -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.continer.id, ViewPostFragment()).commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.chatWithDoctor -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.continer.id,patientsFragment()).commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.sendNotification -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.continer.id, sendNotificationFragment()).commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.addPost -> {
                    supportFragmentManager.beginTransaction().replace(binding.continer.id  , AddPostFragment()).commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.signOutDoc -> {
                    auto.signOut()
                    startActivity(Intent(this , Welcome() :: class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.continer.id, WellcomFragment()).commit()
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