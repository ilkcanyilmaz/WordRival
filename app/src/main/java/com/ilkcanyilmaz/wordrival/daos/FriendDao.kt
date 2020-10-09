package com.ilkcanyilmaz.wordrival.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ilkcanyilmaz.wordrival.models.Friend

@Dao
interface FriendDao {
    @Query("SELECT * FROM tbl_friend")
    fun getFriend(): List<Friend>

    /*  @Query("SELECT * FROM book where name LIKE  :name
              AND author LIKE :author")
          fun findByName(name: String, author: String): User*/

    @Insert
    fun insert(friend: Friend)

    @Delete
    fun delete(friend: Friend)
}