package com.ilkcanyilmaz.wordrival.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ilkcanyilmaz.wordrival.models.User

@Dao
interface UserDao {
    @Query("SELECT * FROM tbl_user")
    fun getUser(): User

    /*  @Query("SELECT * FROM book where name LIKE  :name
              AND author LIKE :author")
          fun findByName(name: String, author: String): User*/

    @Insert
    fun insert(user: User)

    @Delete
    fun delete(user:User)
}