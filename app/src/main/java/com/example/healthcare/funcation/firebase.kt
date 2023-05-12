package com.example.healthcare.funcation

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class firebase {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val postIdMap = mutableMapOf<String, Boolean>()

    fun logScreenViewPost(postID: String) {
        if (postIdMap[postID] == true) {
            Log.d("aaa", "Data already exists for postID $postID")
            return
        }

        val userID = auth.currentUser!!.uid
        firestore.collection("viewPost").document(postID).collection("PostIs")
            .get()
            .addOnSuccessListener { documents ->
                var isTrueOrNot = false
                for (document in documents) {
                    val userIDFromFirestore = document.getString("userID")
                    if (userIDFromFirestore == userID) {
                        isTrueOrNot = true
                        break
                    }
                }
                if (!isTrueOrNot) {
                    val data = hashMapOf(
                        "userID" to userID
                    )
                    firestore.collection("viewPost").document(postID).collection("PostIs")
                        .document(UUID.randomUUID().toString())
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("aaa", "Data added to Firestore for postID $postID")
                            postIdMap[postID] = true
                        }
                } else {
                    Log.d("aaa", "Data already exists for postID $postID")
                    postIdMap[postID] = true
                }
            }
    }

    fun getNameSick(callback: (String) -> Unit) {
        val currentUserId = auth.currentUser!!.uid
        var nameSick: String?
        firestore.collection("sick Information")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { result ->
                nameSick = result.getString("المرض")
                callback(nameSick ?: "")
            }
    }

    fun exitTheApp(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("هل انت متاكد من الخروج من التطبيق ؟")
            .setCancelable(false)
            .setPositiveButton("نعم") { _, _ -> activity.finishAffinity() }
            .setNegativeButton("لا") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    fun convert(callbacks: (String) -> Unit) {
        var name: String?
        getNameSick {
            if (it == "السكري") {
                name = "diabetes"
                callbacks(name ?: "")
            } else if (it == "ارتفاع ضغط الدم") {
                name = "HighBloodPressure"
                callbacks(name ?: "")
            } else if (it == "السمنة") {
                name = "Obesity"
                callbacks(name ?: "")
            } else if (it == "السرطان") {
                name = "cancer"
                callbacks(name ?: "")
            } else if (it == "الكلى") {
                name = "kidneys"
                callbacks(name ?: "")
            } else if (it == "الربو") {
                name = "asthma"
                callbacks(name ?: "")
            } else if (it == "القلب") {
                name = "heart"
                callbacks(name ?: "")
            } else if (it == "Alzheimer") {
                name = "Alzheimer"
                callbacks(name ?: "")
            }
        }
    }

}
