package com.example.healthcare.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get: PropertyName("name") @set:PropertyName("name") var name: String,
    @get: PropertyName("dataOfBirt") @set:PropertyName("dataOfBirt") var dataOfBirt: String,
    @get: PropertyName("address") @set:PropertyName("address") var address: String,
    @get: PropertyName("mobileNumber") @set:PropertyName("mobileNumber") var mobileNumber: String,
    @get: PropertyName("gender") @set:PropertyName("gender") var gender: String,
    @get: PropertyName("profileImage") @set:PropertyName("profileImage") var image: String

)