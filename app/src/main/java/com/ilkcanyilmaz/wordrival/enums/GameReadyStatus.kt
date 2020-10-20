package com.ilkcanyilmaz.wordrival.enums

enum class GameReadyStatus(val value: Int) {
    WAITING(0),
    READY(1),
    REJECTED(2);


    fun getTypeID(): Int {
        return value
    }
}