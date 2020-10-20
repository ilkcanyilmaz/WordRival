package com.ilkcanyilmaz.wordrival.utils

import android.app.Activity
import android.util.Log
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.enums.SendFcmType
import com.ilkcanyilmaz.wordrival.interfaces.FcmInterface
import com.ilkcanyilmaz.wordrival.models.FCMModel
import com.ilkcanyilmaz.wordrival.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
private val BASE_URL = "https://fcm.googleapis.com/fcm/"
val CHAR_SPLIT = "$!"

fun SendFCM(title: String, body: String, token: String, friend: User?, activity: Activity?) {
    var firestoreOperation = FirestoreOperation()
    val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val header = HashMap<String, String>()
    header.put("Content-Type", "application/json")
    header.put(
        "Authorization",
        "key=AAAAxQGfIaU:APA91bFUgF-IofbAGd1bVnkRYQ3DpDcUCS8l2yQmHQIxuK0Aunj2ikHLASjEP-UHGyLj4tct5btNauyCM_04M5DT26BoOOshU0KDR9lYYANMbFJwuePPJrWjQiA0R_SXvUchVWk489I0"
    )

    val data = FCMModel.Data(title, body)
    val notify = FCMModel(
        token,
        data
    )
    val myInterface = retrofit.create(FcmInterface::class.java)
    val request = myInterface.SendFriendRequest(header, notify)
    request.enqueue(object : Callback<Response<FCMModel>> {
        override fun onResponse(
            call: Call<Response<FCMModel>>,
            response: Response<Response<FCMModel>>
        ) {
            if (title == SendFcmType.FRIEND_REQUEST.getTypeID().toString()) {
                firestoreOperation.AddFriend(
                    friend?.userId.toString(),
                    friend?.userNickName.toString(),
                    friend?.userToken.toString(),
                    friend?.userPhoto.toString(),
                    activity
                )
            } else if (title == SendFcmType.FRIEND_REQUEST_RESPONSE.getTypeID().toString()) {
                val bodyData = body.split(CHAR_SPLIT)
                val friendId = friend?.userId.toString()
                val isFriend = bodyData.get(2).toInt()
                firestoreOperation.FriendRequestResponseUpdate(friendId, isFriend)
            }

            Log.d("Retrofit", "Success: $response")
        }

        override fun onFailure(calal: Call<Response<FCMModel>>, t: Throwable) {
            Log.e("Retrofit", "Error: " + t.message)
        }
    })
}