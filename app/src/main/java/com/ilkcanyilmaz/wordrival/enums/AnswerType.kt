package com.ilkcanyilmaz.wordrival.enums

enum class AnswerType(val value: Int) {
    EMPTY(0),
    TRUE(1),
    FALSE(2);


    open fun getTypeID(): Int {
        return value
    }
}