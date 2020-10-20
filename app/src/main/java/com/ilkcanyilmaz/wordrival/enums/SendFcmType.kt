package com.ilkcanyilmaz.wordrival.enums

enum class SendFcmType(val value: Int) {
    FRIEND_REQUEST(1),
    FRIEND_REQUEST_RESPONSE(2),
    FRIEND_REQUEST_GAME(3);


    fun getTypeID(): Int {
        return value
    }
}