package com.ilkcanyilmaz.wordrival

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.functions.FirebaseFunctions
import com.ilkcanyilmaz.wordrival.utils.CHAR_SPLIT

class RandomGameRepository(val data: HashMap<String, Any>?) {
    val gameId = MutableLiveData<String>()

    suspend fun getRandomConnect() {
        FirebaseFunctions.getInstance()
            .getHttpsCallable("randomGameConnect")
            .call(data)
            .continueWith {
                val result = it.result?.data as String
                val isSuccess = result.split(CHAR_SPLIT)[0]
                val gameId = result.split(CHAR_SPLIT)[1]
                if (isSuccess == "success") {
                    this.gameId.value = gameId

                }
            }.addOnFailureListener {
                Log.d("ErrorRandomGameConnect", it.message.toString())
            }
    }
}