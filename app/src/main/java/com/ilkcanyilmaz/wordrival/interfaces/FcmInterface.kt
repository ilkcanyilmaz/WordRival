package com.ilkcanyilmaz.wordrival.interfaces

import com.ilkcanyilmaz.wordrival.models.FCMModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface FcmInterface {
    @POST("send")
    fun SendFriendRequest(
        @HeaderMap headers: Map<String, String>,
        @Body data: FCMModel
    ): Call<Response<FCMModel>>


}