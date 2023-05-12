package com.example.healthcare.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.healthcare.R
import com.example.healthcare.databinding.DeleteLayoutBinding
import com.example.healthcare.databinding.RecaiveMassageBinding
import com.example.healthcare.databinding.SenderMassageBinding
import com.example.healthcare.model.Message
import com.squareup.picasso.Picasso


class MessagesAdapter(
    var context: Context, messages: ArrayList<Message>,
    senderRoom: String, receiverRoom: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    var messages: ArrayList<Message>
    val TIME_SEND = 1
    val TIME_RECEIVE = 2
    var senderRoom: String
    var receiverRoom: String

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SenderMassageBinding = SenderMassageBinding.bind(itemView)

    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RecaiveMassageBinding = RecaiveMassageBinding.bind(itemView)

    }

    init {
        this.messages = messages
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == TIME_SEND){
            val view = LayoutInflater.from(context).inflate(R.layout.sender_massage, parent, false)
            SentMsgHolder(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.recaive_massage, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if(FirebaseAuth.getInstance().uid == message.senderId){
            TIME_SEND
        }else{
            TIME_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.imgSendImage.visibility = View.VISIBLE
                viewHolder.binding.tvSendMessage.visibility = View.GONE
                viewHolder.binding.sendLinear.visibility = View.GONE
                Picasso.get().load(message.imageUri)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(viewHolder.binding.imgSendImage)
            }

            viewHolder.binding.tvSendMessage.text = message.message
            viewHolder.binding.tvMessageTime.text = message.dateSend

            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("حذف الرسالة ؟")
                    .setView(binding.root)
                    .create()

                binding.deleteForEveryOne.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { s ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(senderRoom)
                            .child("message")
                            .child(s).setValue("This message is removed")
                    }
                    message.messageId?.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it2).setValue("This message is removed")
                    }
                    dialog.dismiss()
                }

                binding.deleteForMe.setOnClickListener {
                    message.messageId?.let { q ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(senderRoom)
                            .child("message")
                            .child(q).setValue("This message is removed")
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                true
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.imgReseiceImage.visibility = View.VISIBLE
                viewHolder.binding.tvReseiceMessage.visibility = View.GONE
                viewHolder.binding.reseiceLinear.visibility = View.GONE
                Picasso.get().load(message.imageUri)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(viewHolder.binding.imgReseiceImage)
            }

            viewHolder.binding.tvReseiceMessage.text = message.message
            viewHolder.binding.tvMessageTime.text = message.dateSend

            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete this photo ?")
                    .setView(binding.root)
                    .create()

                binding.deleteForEveryOne.setOnClickListener {
                    message.message = "This photo is removed"
                    message.messageId?.let { z ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(senderRoom)
                            .child("message")
                            .child(z).setValue(message)
                    }
                    message.messageId?.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it2).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.deleteForMe.setOnClickListener {
                    message.messageId?.let { f ->
                        FirebaseDatabase.getInstance().reference.child("Chats")
                            .child(senderRoom)
                            .child("message")
                            .child(f).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }

                false
            }
        }
    }

    override fun getItemCount(): Int = messages.size

}