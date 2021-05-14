package com.ilkcanyilmaz.wordrival.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Game(
    @Exclude
    val gameId: String = "",
    @PropertyName("isDone")
    val isDone: Int = 0,
    @PropertyName("user1Id")
    val user1Id: String = "",
    @PropertyName("user2Id")
    var user2Id: String = "",
    @PropertyName("user1Status")
    var user1Status: Int = 0,
    @PropertyName("user2Status")
    var user2Status: Int = 0,
    @PropertyName("user1Score")
    var user1Score: Int = 0,
    @PropertyName("user2Score")
    var user2Score: Int = 0,
    @PropertyName("user1Time")
    var user1Time: Long = 0,
    @PropertyName("user2Time")
    var user2Time: Long = 0
)
