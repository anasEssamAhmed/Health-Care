package com.example.healthcare.screen.doctor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcare.R
import com.example.healthcare.adapter.DoctorPostView
import com.example.healthcare.adapter.DoctorSendNotification
import com.example.healthcare.databinding.FragmentSendNotificationBinding
import com.example.healthcare.databinding.FragmentViewPostBinding
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.PostForDoctor
import com.example.healthcare.model.notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class sendNotificationFragment : Fragment() {
    lateinit var binding : FragmentSendNotificationBinding
    private val firestore = Firebase.firestore
    private lateinit var adapterMe: DoctorSendNotification
    private var arrayOfData = ArrayList<notification>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSendNotificationBinding.inflate(inflater , container , false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNameUser()
    }

    // get name user
    private fun getNameUser() {
        firebase().getNameSick { nameSick ->
            firestore.collection("users")
                .get().addOnSuccessListener {
                    for (i in it.documents) {
                        val name = i.getString("name")
                        val isSickOrDoctor = i.getString("gender")
                        val idSick = i.id
                        if (isSickOrDoctor == "مريض") {
                            firestore.collection("sick Information").document(idSick)
                                .get()
                                .addOnSuccessListener { s ->
                                    val genderSick = s.getString("المرض")
                                    if (genderSick == nameSick) {
                                        firestore.collection("Token").document(idSick)
                                            .get()
                                            .addOnSuccessListener {a ->
                                                val token = a.getString("token")
                                                if (token != null){
                                                    arrayOfData.add(notification(name!! , idSick))
                                                    adapterMe.notifyDataSetChanged()
                                                }
                                            }


                                    }
                                }
                        }
                    }
                    adapterMe = DoctorSendNotification(requireContext(), arrayOfData)
                    binding.rec.layoutManager = LinearLayoutManager(activity , LinearLayoutManager.VERTICAL , false)
                    binding.rec.adapter = adapterMe
                }
        }
    }
}