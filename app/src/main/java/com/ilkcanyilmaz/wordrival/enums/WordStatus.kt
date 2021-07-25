package com.ilkcanyilmaz.wordrival.enums

enum class WordStatus(val value: Int) {
    STUDIED(1),
    LEARNED(2);

    fun getTypeID(): Int {
        return value
    }

}