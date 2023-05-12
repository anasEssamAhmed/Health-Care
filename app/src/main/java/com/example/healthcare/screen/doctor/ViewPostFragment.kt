package com.example.healthcare.screen.doctor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcare.R
import com.example.healthcare.adapter.DoctorArticles
import com.example.healthcare.adapter.DoctorPostView
import com.example.healthcare.databinding.FragmentViewPostBinding
import com.example.healthcare.model.PostForDoctor
import com.example.healthcare.model.articles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ViewPostFragment : Fragment() {
    lateinit var binding : FragmentViewPostBinding
    private val firestore = Firebase.firestore
    private lateinit var adapterMe: DoctorPostView
    private lateinit var auth: FirebaseAuth
    private var nameDoctor : String? = ""
    private var arrayOfData = ArrayList<PostForDoctor>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentViewPostBinding.inflate(inflater , container , false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        getName()
        getData()
    }

    // get data form firestore
    private fun getData(){
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
                        .addOnSuccessListener {s ->
                            Log.d("name" , nameDoctor.toString())
                            for (i in s.documents) {
                                val title = i.getString("Title")
                                val body = i.getString("Text")
                                val doctorName = i.getString("doctorName")
                                if (doctorName == nameDoctor) {
                                    arrayOfData.add(PostForDoctor( i.id, title!!, body!!))
                                }
                            }
                            binding.rec.layoutManager = LinearLayoutManager(activity , LinearLayoutManager.VERTICAL , false)
                            adapterMe = DoctorPostView(requireContext(), arrayOfData)
                            binding.rec.apply {
                                adapter = adapterMe
                                Log.d("aaa" , arrayOfData.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity , "لا توجد مقالات" , Toast.LENGTH_SHORT).show()
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
}