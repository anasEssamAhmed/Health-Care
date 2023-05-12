package com.example.healthcare.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.databinding.ChatItemBinding
import com.example.healthcare.model.DoctorInfo
import com.example.healthcare.screen.doctor.ChatRoom

class DoctorChat(var activity: Activity,var data : ArrayList<DoctorInfo>) : RecyclerView.Adapter<DoctorChat.ViewHolder>() {
    class ViewHolder(var bindind : ChatItemBinding) : RecyclerView.ViewHolder(bindind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = ChatItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = data[position]
        holder.bindind.DoctorName.text = data[position].name
        holder.bindind.liner.setOnClickListener {
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