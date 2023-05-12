package com.example.healthcare.model

class Message {
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var imageUri: String? = null
    var timeStamp: Long = 0
    var dateSend: String? = null

    constructor(){}
    constructor(message: String?, senderId: String?, time: Long, dateSend: String){
        this.message = message
        this.senderId = senderId
        this.timeStamp = time
        this.dateSend = dateSend
    }
}