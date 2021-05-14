package com.ilkcanyilmaz.wordrival.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.Question

@Dao
interface QuestionDao {
    @Query("SELECT * FROM tbl_question")
    fun getQuestion(): List<Question>

    /*  @Query("SELECT * FROM book where name LIKE  :name
              AND author LIKE :author")
          fun findByName(name: String, author: String): User*/

    @Insert
    fun insert(question: Question)

    @Query("DELETE FROM tbl_question")
    fun delete()
}