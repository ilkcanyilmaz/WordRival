package com.ilkcanyilmaz.wordrival.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "tbl_question")
data class Question(
    @PrimaryKey
    @ColumnInfo(name = "word")
    @PropertyName("word")
    var word: String = "",
    @ColumnInfo(name = "answerTrue")
    @PropertyName("answerTrue")
    val answerTrue: String = "",
    @ColumnInfo(name = "answerFalse1")
    @PropertyName("answerFalse1")
    val answerFalse1: String = "",
    @ColumnInfo(name = "answerFalse2")
    @PropertyName("answerFalse2")
    val answerFalse2: String = "",
    @ColumnInfo(name = "answerFalse3")
    @PropertyName("answerFalse3")
    val answerFalse3: String = "",
    @ColumnInfo(name = "level")
    @PropertyName("level")
    val level: Int? = 0,
    @ColumnInfo(name = "user1Answer")
    @PropertyName("user1Answer")
    var user1Answer: Int? = 0,
    @ColumnInfo(name = "user2Answer")
    @PropertyName("user2Answer")
    var user2Answer: Int? = 0,
)