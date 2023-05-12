package com.example.healthcare.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.databinding.NotificationItemBinding
import com.example.healthcare.model.notification
import com.example.healthcare.screen.doctor.AddPostFragment
import com.example.healthcare.screen.doctor.sendNotificationFragment

class DoctorSendNotification(var context: Context, var data : ArrayList<notification>) : RecyclerView.Adapter<DoctorSendNotification.ViewHolder>() {
    class ViewHolder(var bindind : NotificationItemBinding) : RecyclerView.ViewHolder(bindind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = NotificationItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindind.SickName.text = data[position].name
        holder.bindind.SickItem.setOnClickListener {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val fragment = AddPostFragment.newInstance2(data[position].id)
            fragmentManager.beginTransaction()
                .replace(R.id.continer, fragment)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}