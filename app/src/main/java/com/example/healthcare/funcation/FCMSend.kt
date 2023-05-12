package com.example.healthcare.funcation

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.StrictMode
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.healthcare.R
import org.json.JSONObject


class FCMSend {
    private val BASE_URL = "https://fcm.googleapis.com/fcm/send"
    private val SERVER_KEY: String =
        "key=AAAAsn-eGaQ:APA91bH6dW_ibYzxTwbwYytayb63igEliV-kT3r3I3mvivEDc441mtt9xOU5jtGDCvcWAO6TGP-XrZjyhA1hDjl9bO2IoRsgYbHTYp2oT6d_ocFdVLoHnwrEUjIY1fQBFYKzs9wJZgZ9"

    fun send(context: Context, title: String, massage: String , token:String?) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val requestQueue = Volley.newRequestQueue(context)
        try {
            val json = JSONObject()
            firebase().convert {
                if (token != null) {
                    json.put("to", token)
                    Log.d("aaa" , "this is token in the fcm : $token")
                }else {
                    json.put("to", "/topics/$it")
                    Log.d("aaa" , "this is token in the fcm else : $token")

                }
                val json2 = JSONObject()
                json2.put("title", title)
                json2.put("body", massage)
                json2.put("icon", "baseline_notifications_active_24")
                json2.put("sound", "default")
                json.put("notification", json2)
                val jsonObjectRequest =
                    object : JsonObjectRequest(Request.Method.POST, BASE_URL, json, {
                        Log.d("aaa", it.toString())
                    }, { error -> Log.d("aaa", error.message.toString()) }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val map = HashMap<String, String>()
                            map["Content-Type"] = "application/json"
                            map["Authorization"] = SERVER_KEY
                            return map
                        }
                    }
                requestQueue.add(jsonObjectRequest)
            }
        } catch (e: Exception) {
            Log.d("aaa", e.message.toString())
        }
    }
}