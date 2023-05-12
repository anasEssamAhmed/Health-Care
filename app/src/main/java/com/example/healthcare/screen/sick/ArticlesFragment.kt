package com.example.healthcare.screen.sick

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcare.adapter.DoctorArticles
import com.example.healthcare.databinding.FragmentArticlesBinding
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.articles
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ArticlesFragment : Fragment() {
    private lateinit var binding : FragmentArticlesBinding
    private var adapter: DoctorArticles? = null
    private var firestore  = Firebase.firestore
    private var auth = Firebase.auth
    private var array = ArrayList<articles>()
    private var nameSick : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    // get Data from firestore
    private fun getData(){
        // check if the fragment is attached to the activity
        if (!isAdded) return
        val currentUserId = auth.currentUser!!.uid
        firestore.collection("sick Information")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { s ->
                Log.d("aaa" , "number i")
                nameSick = s.getString("المرض")
                if (nameSick == null) {
                    Log.d("aaa", "No Data")
                } else {
                    firestore.collection("posts-$nameSick")
                        .get()
                        .addOnSuccessListener {result ->
                            for (i in result.documents) {
                                val doctorName = i.getString("doctorName")
                                val title = i.getString("Title")
                                val text = i.getString("Text")
                                val time = i.getString("date")
                                val isShow = i.getBoolean("isShow")
                                val id = i.id
                                val photoID = i.getString("photoId")
                                val videoID = i.getString("videoId")
                                if (isShow == true){
                                    array.add(articles(title!!, doctorName!!, time!!, text!! , photoID , videoID))
                                    firebase().logScreenViewPost(id)
                                }
                            }
                            binding.rec.layoutManager = LinearLayoutManager(activity , LinearLayoutManager.VERTICAL , false)
                            adapter = DoctorArticles(requireContext() , array)
                            binding.rec.adapter = adapter
                        }

                }
            }
    }

}