package com.example.healthcare.screen

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import com.example.healthcare.R
import com.example.healthcare.databinding.SignUpScreenBinding
import com.example.healthcare.funcation.Massage
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class signUp : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: SignUpScreenBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = Firebase.firestore
    private var filePhoto: Uri? = null
    private var filePhotoInFirebase: String? = null
    private val REQUEST_CODE_SELECT_PHOTO = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTO)
        }
        checkConnected()
        binding.dateOfBirthEdittext.setOnClickListener {
            showDatePickerDialog()
            hideKeyboard()
        }
        auth = Firebase.auth
        // click button to create account
        binding.createAccountButton.setOnClickListener {
            var isEmpty = binding.nameEdittext.text!!.isNotEmpty()
                    && binding.dateOfBirthEdittext.text!!.isNotEmpty()
                    && binding.addressEdittext.text!!.isNotEmpty()
                    && binding.emailEdittext.text!!.isNotEmpty()
                    && binding.mobileNumberEdittext.text!!.isNotEmpty()
                    && binding.passwordEdittext.text!!.isNotEmpty()
                    && binding.confirmPasswordEdittext.text!!.isNotEmpty()
                    && binding.radioGroup.isNotEmpty()
                    && filePhoto != null
            if (isEmpty) {
                if (binding.passwordEdittext.text.toString() == binding.confirmPasswordEdittext.text.toString()) {
                    if (binding.radioButtonDoctor.isChecked || binding.radioButtonSick.isChecked) {
                        if (binding.checkBoxPrivcy.isChecked) {
                            createAccount(
                                binding.emailEdittext.text.toString(),
                                binding.passwordEdittext.text.toString()
                            )
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "يجب الموافقة على استخدام المعلومات الخاصة بك من اجل انشاء حساب لك",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "يجب اختيار النوع : هل انت طبيب ام مريض ؟",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext, "يجب تطابق كلمة المرور", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext, "يرجى تعبئة جميع الحقول", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val selectedDate = "$p3/${p2 + 1}/$p1"
        binding.dateOfBirthEdittext.setText(selectedDate)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this, this, year, month, dayOfMonth)
        datePickerDialog.show()
    }

    // Hide keyboard
    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    //Create account
    private fun createAccount(email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("جاري التسجيل")
        progressDialog.setMessage("رجاء قم بالانتظار ...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        createInforamtion(email, password, progressDialog)
    }

fun checkConnected() {
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


override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
        if (data?.data == null) {
            Toast.makeText(this, "يوجد مشكلة قم برفع صورة اخرى", Toast.LENGTH_SHORT).show()
            Picasso.get().load(filePhoto).placeholder(R.drawable.upload)
                .into(binding.imageProfile)

        } else {
            filePhoto = data.data
            Picasso.get().load(filePhoto).placeholder(R.drawable.upload)
                .into(binding.imageProfile)
        }
    }
}

private fun createInforamtion(email: String, password: String, progressDialog: ProgressDialog) {
        checkConnected()
        val randomId = UUID.randomUUID().toString()
        val ref =
            FirebaseStorage.getInstance().reference.child("Profile").child(randomId)
        ref.putFile(filePhoto!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                filePhotoInFirebase = it.toString()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "تم التسجيل بنجاح",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            //references firestore
                            val ref = firestore.collection("users")
                                .document(auth.currentUser!!.uid)
                            val name = binding.nameEdittext.text.toString()
                            val mobileNumber =
                                binding.mobileNumberEdittext.text.toString()
                            var gender = ""
                            if (binding.radioButtonDoctor.isChecked) {
                                gender = "طبيب"
                            } else {
                                gender = "مريض"
                            }

                            val user = User(
                                name,
                                binding.dateOfBirthEdittext.text.toString(),
                                binding.addressEdittext.text.toString(),
                                mobileNumber,
                                gender,
                                filePhotoInFirebase!!
                            )
                            ref.set(user)
                                .addOnSuccessListener {
                                    Log.d("ooo", "Success to add to firestore")
                                    val mFirebaseAnalytics =
                                        FirebaseAnalytics.getInstance(this)
                                    // Log the user account creation event
                                    val accountCreateBundle = Bundle()
                                    accountCreateBundle.putString(
                                        FirebaseAnalytics.Param.METHOD,
                                        email
                                    )
                                    mFirebaseAnalytics.logEvent(
                                        FirebaseAnalytics.Event.SIGN_UP,
                                        accountCreateBundle
                                    )
                                    progressDialog.dismiss()
                                    val i = Intent(this, SelectDisease::class.java)
                                    startActivity(i)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Log.d("ooo", "Failure to add to firestore")
                                    progressDialog.dismiss()
                                }
                        } else {
                            progressDialog.dismiss()
                            val exception = task.exception.toString().split(":")
                            Log.d("ooo", exception[1])
                            if (exception[1] == " The email address is badly formatted.") {
                                Toast.makeText(
                                    applicationContext,
                                    "يرجى التاكد من البريد الالكتروني",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            if (exception[1] == " The email address is already in use by another account.") {
                                Toast.makeText(
                                    applicationContext,
                                    "البريد الالكتروني مسجل بالفعل",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (exception[1] == " The given password is invalid. [ Password should be at least 6 characters ]") {
                                Toast.makeText(
                                    applicationContext,
                                    "يرجى استخدام على الاقل 6 حروف في كلمة المرور",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                    }
            }.addOnFailureListener {
                progressDialog.dismiss()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
        }
}

}