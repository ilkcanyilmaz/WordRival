package com.ilkcanyilmaz.wordrival.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    var userId: String = "",
    @ColumnInfo(name = "userToken")
    var userToken: String = "",
    @ColumnInfo(name = "userMail")
    var userMail: String = "",
    @ColumnInfo(name = "userNickName")
    var userNickName: String = "",
    @ColumnInfo(name = "userName")
    var userName: String = "",
    @ColumnInfo(name = "userScore")
    var userScore: Int = 0,
    @ColumnInfo(name = "userLevel")
    var userLevel: Int = 0,
    @ColumnInfo(name = "userPhoto")
    var userPhoto: String = "0"
)