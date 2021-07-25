package com.ilkcanyilmaz.wordrival.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "tbl_word")
data class WordModel(
    @PrimaryKey
    @ColumnInfo(name = "questionId")
    @PropertyName("questionId")
    val questionId: String = "",
    @ColumnInfo(name = "status")
    @PropertyName("status")
    val status: Int = 0,
)