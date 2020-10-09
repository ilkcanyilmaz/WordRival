package com.ilkcanyilmaz.wordrival.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_friend")
data class Friend(
   @ColumnInfo(name = "friendUserId")
   var friendId: String = "",
   @ColumnInfo(name = "friendToken")
   var friendToken: String = "",
   @PrimaryKey
   @ColumnInfo(name = "friendMail")
   var friendMail: String = "",
   @ColumnInfo(name = "friendNickName")
   var friendNickName: String = "",
   @ColumnInfo(name = "friendFullName")
   var friendFullName: String = "",
   @ColumnInfo(name = "friendPhotoUrl")
   var friendPhotoUrl: String = "",
   @ColumnInfo(name = "isFriend")
   var isFriend: Int = 5 // 0: Arkadaşlık isteği onaylanmadı, 1: Arkadaşlık isteği onaylandı,
) {

}
