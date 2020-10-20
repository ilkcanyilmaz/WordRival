package com.ilkcanyilmaz.wordrival.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Question(
    @Exclude
    val id: Int = 0,
    @PropertyName("isDone")
    val level: Boolean = false,
    @PropertyName("user1Id")
    val user1Id: String = "",
    @PropertyName("user2Id")
    var user2Id: String = "",
    @PropertyName("isUser1Ready")
    var isUser1Ready: Boolean = false,
    @PropertyName("isUser2Ready")
    var isUser2Ready: Boolean = false
)
