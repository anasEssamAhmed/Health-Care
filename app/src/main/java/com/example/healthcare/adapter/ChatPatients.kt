package com.example.healthcare.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.databinding.NotificationItemBinding
import com.example.healthcare.model.DoctorInfo
import com.example.healthcare.model.notification
import com.example.healthcare.screen.doctor.ChatRoom

class ChatPatients(var activity: Activity, var data : ArrayList<DoctorInfo>) : RecyclerView.Adapter<ChatPatients.ViewHolder>() {
    class ViewHolder(var bindind : NotificationItemBinding) : RecyclerView.ViewHolder(bindind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = NotificationItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = data[position]
        holder.bindind.SickName.text = user.name
        holder.bindind.SickItem.setOnClickListener {
            val intent = Intent(activity, ChatRoom::class.java)
            intent.putExtra("userId", user.UserId)
            intent.putExtra("userImage", user.image)
            intent.putExtra("userName", user.name)
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}