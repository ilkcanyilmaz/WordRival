package com.ilkcanyilmaz.wordrival.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "tbl_user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    @Exclude()
    var userId: String = "",
    @ColumnInfo(name = "userToken")
    @PropertyName("userToken")
    var userToken: String = "",
    @ColumnInfo(name = "userMail")
    @PropertyName("userMail")
    var userMail: String = "",
    @ColumnInfo(name = "userNickName")
    @PropertyName("userNickName")
    var userNickName: String = "",
    @ColumnInfo(name = "userName")
    @PropertyName("userFullName")
    var userName: String = "",
    @ColumnInfo(name = "userScore")
    @PropertyName("userScore")
    var userScore: Int = 0,
    @ColumnInfo(name = "userPhoto")
    @PropertyName("userPhoto")
    var userPhoto: String = "",
    @ColumnInfo(name = "userOfflineRecord")
    @PropertyName("userOfflineRecord")
    var userOfflineRecord: Int = 0
)