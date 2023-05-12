package com.example.healthcare.screen.sick

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcare.adapter.DoctorChat
import com.example.healthcare.databinding.FragmentAvailableDoctorsBinding
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.DoctorInfo
import com.example.healthcare.screen.doctor.ChatRoom
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AvailableDoctors : Fragment() {
    private lateinit var binding : FragmentAvailableDoctorsBinding
    private lateinit var adapter: DoctorChat
    private var firestore = Firebase.firestore
    val arrayOfData = ArrayList<DoctorInfo>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAvailableDoctorsBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNameDoctor()
    }

    private fun getNameDoctor(){
        firebase().getNameSick { nameSick ->
            firestore.collection("users")
                .get().addOnSuccessListener {
                    for (i in it.documents) {
                        val name = i.getString("name")
                        val isSickOrDoctor = i.getString("gender")
                        val idSick = i.id
                        val photo = i.getString("profileImage")
                        if (isSickOrDoctor == "طبيب") {
                            firestore.collection("sick Information").document(idSick)
                                .get()
                                .addOnSuccessListener { s ->
                                    val genderSick = s.getString("المرض")
                                    if (genderSick == nameSick) {
                                        firestore.collection("Token").document(idSick)
                                            .get()
                                            .addOnSuccessListener {
                                                arrayOfData.add(DoctorInfo(name!! , idSick , photo!!))
                                                    adapter.notifyDataSetChanged()
                                            }


                                    }
                                }
                        }
                    }
                    val activity: Activity? = activity
                    adapter = DoctorChat(activity!! , arrayOfData)
                    binding.rec.layoutManager = LinearLayoutManager(activity , LinearLayoutManager.VERTICAL , false)
                    binding.rec.adapter = adapter
                }
        }
    }
}