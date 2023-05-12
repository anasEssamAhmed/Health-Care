package com.example.healthcare.screen.sick

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.healthcare.R
import com.example.healthcare.databinding.FragmentProfileBinding
import com.example.healthcare.screen.SelectDisease
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class Profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val firestore = Firebase.firestore
    private var auth = Firebase.auth
    private val randomId = UUID.randomUUID().toString()
    private var filePhoto : Uri? = null
    private var filePhotoInFirebase : String? = null
    private val REQUEST_CODE_SELECT_PHOTO = 200
    private val refs = FirebaseStorage.getInstance().reference.child("Profile").child(randomId)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showInfoFromFirestore()
        binding.imageViewProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTO)
        }
        binding.saveButton1.setOnClickListener {
            if (binding.saveButton1.text == "تعديل") {
                binding.saveButton1.text = "حفظ"
                binding.emailTextFieldProfile1.isEnabled = true
                binding.addressTextFieldProfile1.isEnabled = true
                binding.phoneNumberTextFieldProfile1.isEnabled = true
            } else {
                if (binding.emailTextFieldProfile1.text!!.isNotEmpty()&& binding.addressTextFieldProfile1.text!!.isNotEmpty() && binding.phoneNumberTextFieldProfile1.text!!.isNotEmpty() && filePhoto != null){
                    updateInfo()
                    showInfoFromFirestore()
                    binding.saveButton1.text = "تعديل"
                }else {
                    Toast.makeText(activity , "رجاء قم بتعبئة جميع الحقول" , Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.changeDiseaseButton.setOnClickListener {
            changeDisease()
        }
        binding.changePasswordButton.setOnClickListener {
            if (auth.currentUser?.email != null) {
                auth.sendPasswordResetEmail(auth.currentUser!!.email!!)
                Toast.makeText(activity , "تم ارسال رابط تغيير كلمة المرور الى البريد الالكتروني" , Toast.LENGTH_LONG).show()
            }
        }
    }

    // update info in the firestore
    private fun updateInfo() {
        val currentUser = auth.currentUser
        var ref = firestore.collection("users").document(currentUser!!.uid)
        ref.update("address" , binding.addressTextFieldProfile1.text.toString()).addOnFailureListener {es ->
            Log.d("aaa" , "this is address : ${es.message.toString()}")

        }
        ref.update("mobileNumber" , binding.phoneNumberTextFieldProfile1.text.toString()).addOnFailureListener { ex ->
            Log.d("aaa" , "this is mobile number : ${ex.message.toString()}")
        }
        refs.putFile(filePhoto!!).addOnSuccessListener {
            refs.downloadUrl.addOnSuccessListener {
                filePhotoInFirebase = it.toString()
                ref.update("profileImage" , filePhotoInFirebase)
                Toast.makeText(activity, "تم عمل تحديث بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
        currentUser.updateEmail(binding.emailTextFieldProfile1.text.toString()).addOnFailureListener {
            Log.d("aaa" , it.message.toString())
        }
        showInfoFromFirestore()
    }

    // show information from firestore in database

    private fun showInfoFromFirestore() {
        val currentUser = auth.currentUser!!.uid
        Log.d("ooo : this is current User", currentUser)
        firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener { result ->
                val address = result.getString("address")
                val phoneNumber = result.getString("mobileNumber")
                val email = auth.currentUser!!.email
                val photo = result.getString("profileImage")
                binding.emailTextFieldProfile1.setText(email)
                binding.addressTextFieldProfile1.setText(address)
                binding.phoneNumberTextFieldProfile1.setText("$phoneNumber")
                binding.emailTextFieldProfile1.isEnabled = false
                binding.addressTextFieldProfile1.isEnabled = false
                binding.phoneNumberTextFieldProfile1.isEnabled = false
                Picasso.get().load(photo).placeholder(R.drawable.upload).into(binding.imageViewProfile)

            }

    }

    // change the disease
    private fun changeDisease() {
        val i = Intent(activity, SelectDisease::class.java)
        i.putExtra("isTrueOrNot", true)
        activity?.startActivity(i)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            filePhoto = data?.data
            Picasso.get().load(filePhoto).placeholder(R.drawable.upload).into(binding.imageViewProfile)
        }
    }

}