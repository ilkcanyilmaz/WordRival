package com.ilkcanyilmaz.wordrival.enums

enum class FriendRequestType(val value: Int) {
    WAITING(0),
    ACCEPT(1),
    REJECTED(2);


    open fun getTypeID(): Int {
        return value
    }
}