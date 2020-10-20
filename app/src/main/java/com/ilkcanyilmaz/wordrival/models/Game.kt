package com.ilkcanyilmaz.wordrival.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Game(
    @Exclude
    val gameId: String = "",
    @PropertyName("isDone")
    val isDone: Boolean = false,
    @PropertyName("user1Id")
    val user1Id: String = "",
    @PropertyName("user2Id")
    var user2Id: String = "",
    @PropertyName("isUser1Ready")
    var isUser1Ready: Boolean = false,
    @PropertyName("isUser2Ready")
    var isUser2Ready: Boolean = false
)

data class Questions(
    @PropertyName("word")
    val word: String = "",
    @PropertyName("answerTrue")
    val answerTrue: String = "",
    @PropertyName("answerFalse1")
    val answerFalse1: String = "",
    @PropertyName("answerFalse2")
    val answerFalse2: String = "",
    @PropertyName("answerFalse3")
    val answerFalse3: String = "",
    @PropertyName("level")
    val level: Int? = 0,
    @PropertyName("user1Answer")
    val user1Answer: Int? = 0,
    @PropertyName("user2Answer")
    val user2Answer: Int? = 0,
)