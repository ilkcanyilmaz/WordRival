package com.ilkcanyilmaz.wordrival.enums

enum class UserType(val value: Int) {
    USER1(1),
    USER2(2);


    fun getTypeID(): Int {
        return value
    }
}