package com.example.healthcare.screen.doctor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcare.databinding.FragmentAddPostBinding
import com.example.healthcare.funcation.FCMSend
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class AddPostFragment : Fragment() {
    lateinit var binding: FragmentAddPostBinding
    private val firestore = Firebase.firestore
    private  var auth =  Firebase.auth
    private var title: String? = ""
    private var text: String? = ""
    private var numberIs: String = ""
    private var count = 0
    private var nameDoctor : String? = ""
    private var time  = ""
    private var userId: String? = ""
    private var fileUriVideo : Uri? = null
    private var fileUriPhoto : Uri? = null
    private var REQUEST_CODE_SELECT_VIDEO = 200
    private var REQUEST_CODE_SELECT_PHOTO = 400
    private var randomUidForVideo = UUID.randomUUID().toString()
    private var randomUidForPhoto = UUID.randomUUID().toString()
    private var fileVieoInTheFirebase : String? = null
    private var filePhotoInTheFirebase : String? = null
    companion object {
        fun newInstance(postTitle: String, postContent: String): AddPostFragment {
            val args = Bundle()
            args.putString("post_title", postTitle)
            args.putString("post_content", postContent)
            val fragment = AddPostFragment()
            fragment.arguments = args
            return fragment
        }
        fun newInstance2(userId: String): AddPostFragment {
            val args = Bundle()
            args.putString("userID", userId)
            val fragment = AddPostFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        title = arguments?.getString("post_title")
        text = arguments?.getString("post_content")
        if (title != null && text != null) {
            binding.editTextTextTitle.setText(title)
            binding.editTextText.setText(text)
            binding.PostButton.text = "حفظ التحديث"
            binding.adviseButton.isEnabled = false
            binding.textViewAddPost.text = "قم بتحديث المقالة"
            count = 1
        }
        userId = arguments?.getString("userID")
        if (userId != null) {
            binding.PostButton.isEnabled = false
            binding.addPhoto.isEnabled = false
            binding.addVideo.isEnabled = false
        }
        getName()
        getTime()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.PostButton.setOnClickListener {
            if (binding.editTextText.text.isNotEmpty()) {
                if (count != 0){
                    // number 5 is the video only
                    if (fileUriVideo != null && fileUriPhoto == null) {
                        addVideoToStorage(20)
                        Log.d("aaa" , "this is only video")
                    }
                    // photo not null but video null
                    if (fileUriPhoto != null && fileUriVideo == null){
                        addPhotoToStorage(20)
                        Log.d("aaa" , "this is only photo")

                    }
                    // video and photo are not null
                    if (fileUriPhoto != null && fileUriVideo != null){
                        addVideoToStorage(30)
                        Log.d("aaa" , "this is video and photo")
                    }
                    // video and photo are null
                    if (fileUriPhoto == null && fileUriVideo == null){
                        updateData()
                        Log.d("aaa" , "this is both null")
                    }
                }else {
                    // number 5 is the video only
                    if (fileUriVideo != null && fileUriPhoto == null) {
                        addVideoToStorage(5)
                        Log.d("aaa" , "this is only video")
                    }
                    // photo not null but video null
                    if (fileUriPhoto != null && fileUriVideo == null){
                        addPhotoToStorage(10)
                        Log.d("aaa" , "this is only photo")

                    }
                    // video and photo are not null
                    if (fileUriPhoto != null && fileUriVideo != null){
                        addVideoToStorage(10)
                        Log.d("aaa" , "this is video and photo")
                    }
                    // video and photo are null
                    if (fileUriPhoto == null && fileUriVideo == null){
                        saveData()
                        Log.d("aaa" , "this is both null")
                    }
                }
            } else {
                Toast.makeText(activity, "رجاء قم بكتابة المقالة", Toast.LENGTH_SHORT).show()
            }
        }

        binding.adviseButton.setOnClickListener {
            if (binding.editTextTextTitle.text.isNotEmpty() && binding.editTextText.text.isNotEmpty()){
                if (userId != null) {
                    getTokenUserFromFirestoreAndPostNotification()
                }else {
                    FCMSend().send(requireContext(),binding.editTextTextTitle.text.toString() , binding.editTextText.text.toString() , null)
                    Toast.makeText(activity , "تم الارسال بنجاح" , Toast.LENGTH_SHORT).show()
                }
            }else {
                Toast.makeText(activity , "رجاء قم بتعبئة النص", Toast.LENGTH_SHORT).show()
            }
        }

        binding.addVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
        }
        binding.addPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTO)
        }
    }

    // save Data in the firestore
    private fun saveData() {
        val currentUserId = auth.currentUser
        // we need to access to some information
        firestore.collection("sick Information")
            .document(currentUserId!!.uid)
            .get()
            .addOnSuccessListener { result ->
                val nameSick: String? = result.getString("المرض")
                if (nameSick == null) {
                    Log.d("aaa", "No Data")
                } else {
                    Log.d("aaa", nameSick)
                    // now you can to save post data in the firestore
                    try {
                        val mapData = mapOf(
                            "doctorName" to nameDoctor,
                            "Title" to binding.editTextTextTitle.text.toString(),
                            "Text" to binding.editTextText.text.toString(),
                            "date" to time,
                            "isShow" to true,
                            "videoId" to fileVieoInTheFirebase,
                            "photoId" to filePhotoInTheFirebase

                        )
                        Log.d("aaa", "this is uri video save data: $fileVieoInTheFirebase")
                        Log.d("aaa", "this is uri photo save data: $filePhotoInTheFirebase")
                        firestore.collection("posts-$nameSick")
                            .document(UUID.randomUUID().toString())
                            .set(mapData)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "تم نشر المقالة بنجاح", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    activity,
                                    "حدث خطا قم بالمحاولة في وقت لاحق",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }catch (e : Exception){
                        Log.d("aaa" , e.message.toString())
                    }
                }

            }
    }

    private fun updateData() {
        val currentUserId = auth.currentUser!!.uid
        firestore.collection("sick Information")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val nameSick: String? = result.getString("المرض")
                if (nameSick == null) {
                    Log.d("aaa", "No Data")
                } else {
                    firestore.collection("posts-$nameSick")
                        .get()
                        .addOnSuccessListener { s ->
                            for (i in s.documents) {
                                val titles = i.getString("Title")
                                val texts = i.getString("Text")
                                val doctorName = i.getString("doctorName")
                                if (titles == title && texts == text) {
                                    numberIs = i.id
                                    val mapData = mapOf(
                                        "doctorName" to doctorName,
                                        "Title" to binding.editTextTextTitle.text.toString(),
                                        "Text" to binding.editTextText.text.toString(),
                                        "date" to time,
                                        "isShow" to true,
                                        "videoId" to fileVieoInTheFirebase,
                                        "photoId" to filePhotoInTheFirebase
                                    )
                                    firestore.collection("posts-$nameSick").document(numberIs).update(mapData)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity , "تم التحديث بنجاح" , Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(activity , "لقد حدث خطا , قم بالمحاولة في وقت لاحق" , Toast.LENGTH_SHORT).show()

                                        }
                                    break
                                }
                            }
                        }
                }
            }
    }

    private fun getName() {
        firestore.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                nameDoctor = it.getString("name")
            }
    }

    private fun getTime(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Note: Calendar.MONTH starts from 0
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        Log.d("time" , hour.toString())
        if (hour > 12){
            hour -= 12
            time = "$year/$month/$day - $hour pm"
        }else if (hour == 12) {
            time = "$year/$month/$day - $hour pm"
        }else if (hour == 0){
            hour = 12
            time = "$year/$month/$day - $hour am"
        }else {
            time = "$year/$month/$day - $hour am"
        }
    }

    // get Token User From Firestore and post the notification
    private fun getTokenUserFromFirestoreAndPostNotification(){
        firestore.collection("Token").document(userId!!)
            .get()
            .addOnSuccessListener {
                val token = it.getString("token")
                Log.d("aaa" , "This is Token : $token")
                FCMSend().send(requireContext(),binding.editTextTextTitle.text.toString() , binding.editTextText.text.toString() , token)
                Toast.makeText(activity , "تم الارسال بنجاح" , Toast.LENGTH_SHORT).show()
            }
    }

    // add video in the firebase storage
    private fun addVideoToStorage(num : Int){
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("videos")
        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("جاري الرفع")
        progressDialog.setMessage("رجاء قم بالانتظار ...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        storageRef.child(randomUidForVideo).putFile(fileUriVideo!!)
            .addOnSuccessListener {
                Log.d("aaa" , "success add video")
            storageRef.child(randomUidForVideo).downloadUrl.addOnSuccessListener { uri ->
                fileVieoInTheFirebase = uri.toString()
                Log.d("aaa" , "this is uri in the download")
                if (num == 5){
                    saveData()
                    Log.d("aaa" , "this is uri : $fileVieoInTheFirebase")
                    progressDialog.dismiss()
                }else if (num == 10){
                    addPhotoToStorage(10)
                    progressDialog.dismiss()
                }else if(num == 20){
                    updateData()
                    progressDialog.dismiss()
                }else if(num == 30){
                    addPhotoToStorage(20)
                    progressDialog.dismiss()
                }

            }
            }.addOnFailureListener{
                progressDialog.dismiss()
            }
    }
    private fun addPhotoToStorage(num: Int) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("photo")
        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("جاري الرفع")
        progressDialog.setMessage("رجاء قم بالانتظار ...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        storageRef.child(randomUidForPhoto).putFile(fileUriPhoto!!).addOnSuccessListener {
                Log.d("aaa" , "success add photo")
            storageRef.child(randomUidForPhoto).downloadUrl.addOnSuccessListener { uri ->
                filePhotoInTheFirebase = uri.toString()
                Log.d("aaa" , "this is uri $filePhotoInTheFirebase")
                if (num == 20){
                    updateData()
                    progressDialog.dismiss()
                }else if (num == 10) {
                    saveData()
                    progressDialog.dismiss()
                }
            }
            }.addOnFailureListener{
                progressDialog.dismiss()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == Activity.RESULT_OK) {
            fileUriVideo = data?.data
        }
        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            fileUriPhoto = data?.data
        }
    }
}