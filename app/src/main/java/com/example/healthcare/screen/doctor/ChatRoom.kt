package com.example.healthcare.screen.doctor


import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.example.healthcare.R
import com.example.healthcare.adapter.MessagesAdapter
import com.example.healthcare.databinding.ActivityChatRoomBinding
import com.example.healthcare.funcation.FCMSend
import com.example.healthcare.funcation.Massage
import com.example.healthcare.model.Message
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatRoom : AppCompatActivity() {
    lateinit var binding: ActivityChatRoomBinding
    lateinit var messagesAdapter: MessagesAdapter
    lateinit var senderRoom: String
    lateinit var receiverRoom: String
    lateinit var senderUserId: String
    lateinit var receiverUserId: String
    var firestore = Firebase.firestore
    var messages = ArrayList<Message>()
    lateinit var database: FirebaseDatabase
    val storage = FirebaseStorage.getInstance()
    lateinit var dialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance("https://health-care-4ed55-default-rtdb.firebaseio.com/")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        dialog = ProgressDialog(this@ChatRoom)
        dialog.setMessage("يتم رفع الصورة الان ...")
        dialog.setCancelable(false)

        val userImage = intent.getStringExtra("userImage").toString()
        val userName = intent.getStringExtra("userName").toString()

        receiverUserId = intent.getStringExtra("userId").toString()
        senderUserId = FirebaseAuth.getInstance().uid.toString()

        binding.myName.text = userName
        Picasso.get().load(userImage).placeholder(R.drawable.baseline_person_24).into(binding.imgProfileImage)

        binding.rowBack.setOnClickListener {
            super.onBackPressed()
        }

        database.reference.child("Presence").child(receiverUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if(status.equals("غير متصل")){
                            binding.state.visibility = View.INVISIBLE
                        }else{
                            binding.state.text = status
                            binding.state.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        senderRoom = senderUserId + receiverUserId
        receiverRoom = receiverUserId + senderUserId
        messagesAdapter = MessagesAdapter(this@ChatRoom, messages, senderRoom, receiverRoom)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this@ChatRoom)
        binding.messageRecyclerView.adapter = messagesAdapter
        database.reference.child("Chats").child(senderRoom).child("message")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for(snap in snapshot.children){
                        try {
                            val message: Message? = snap.getValue(Message::class.java)
                            message!!.messageId = snap.key
                            messages.add(message)
                        }catch (e : Exception){
                            Log.d("aaa" , e.message.toString())
                        }
                    }
                    messagesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.etMessage.addTextChangedListener {
            val message = binding.etMessage.text.toString().trim()
            if(message.isEmpty()){
                binding.imgSend.isClickable = false
            }else{
                binding.imgSend.isClickable = true
            }
        }

        binding.imgSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if(messageText.isNotEmpty()) {
                val simpleDate = SimpleDateFormat("hh:mm - dd/MM/yyyy")
                val dateSend = simpleDate.format(Date())
                val date = Date()
                val message = Message(messageText, senderUserId, date.time, dateSend)

                binding.etMessage.setText("")
                val randomKey = database.reference.push().key
                val lastMessageObj = HashMap<String, Any>()
                lastMessageObj["lastMessage"] = message.message!!
                lastMessageObj["lastMessageTime"] = date.time
                firestore.collection("Token").document(receiverUserId).get().addOnSuccessListener {
                    val token = it.getString("token")
                    firestore.collection("users").document(senderUserId).get().addOnSuccessListener {
                        var name = it.getString("name")
                        FCMSend().send(this@ChatRoom , "رسالة من $name" , messageText , token)

                    }
                }

                database.reference.child("Chats").child(senderRoom)
                    .updateChildren(lastMessageObj)
                database.reference.child("Chats").child(receiverRoom)
                    .updateChildren(lastMessageObj)
                database.reference.child("Chats").child(senderRoom)
                    .child("message")
                    .child(randomKey!!)
                    .setValue(message)
                    .addOnSuccessListener {
                        database.reference.child("Chats").child(receiverRoom)
                            .child("message")
                            .child(randomKey)
                            .setValue(message)
                            .addOnSuccessListener {
                                Log.e("msa", "Line 144")
                            }
                    }
            }
        }

        binding.imgAttachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding.etMessage.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                database.reference.child("Presence").child(senderUserId)
                    .setValue("يكتب ...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedYyping, 1000)
            }
            var userStoppedYyping = Runnable {
                database.reference.child("Presence").child(senderUserId)
                    .setValue("متصل")
            }
        })
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25){
            if(data != null){
                if(data.data != null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val refence = storage.reference.child("Chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog.show()
                    refence.putFile(selectedImage!!)
                        .addOnCompleteListener{ task ->
                            dialog.dismiss()
                            if(task.isSuccessful){
                                refence.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageText = binding.etMessage.text.toString().trim()
                                    val simpleDate = SimpleDateFormat("hh:mm - dd/MM/yyyy")
                                    val dateSend = simpleDate.format(Date())
                                    val date = Date()
                                    val message = Message(messageText, senderUserId, date.time, dateSend)
                                    message.message = "photo"
                                    message.imageUri = filePath
                                    binding.etMessage.setText("")

                                    val randomKey = database.reference.push().key
                                    val lastMessageObj = HashMap<String, Any>()
                                    lastMessageObj["lastMessage"] = message.message!!
                                    lastMessageObj["lastMessageTime"] = date.time
                                    database.reference.child("Chats")
                                        .updateChildren(lastMessageObj)
                                    database.reference.child("Chats")
                                        .child(receiverRoom)
                                        .updateChildren(lastMessageObj)
                                    database.reference.child("Chats")
                                        .child(senderRoom)
                                        .child("message")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database.reference.child("Chats")
                                                .child(receiverRoom)
                                                .child("message")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener {
                                                    Log.e("msa", "Line 223")
                                                }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        database = FirebaseDatabase.getInstance()
        senderUserId = FirebaseAuth.getInstance().uid.toString()
        database.reference.child("Presence").child(senderUserId).setValue("متصل")
    }

    override fun onPause() {
        super.onPause()
        database = FirebaseDatabase.getInstance()
        senderUserId = FirebaseAuth.getInstance().uid.toString()
        database.reference.child("Presence").child(senderUserId).setValue("غير متصل")
    }
}