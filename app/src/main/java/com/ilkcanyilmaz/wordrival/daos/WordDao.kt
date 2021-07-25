package com.ilkcanyilmaz.wordrival.daos

import androidx.room.*
import com.ilkcanyilmaz.wordrival.models.WordModel

@Dao
interface WordDao {
    @Query("SELECT * FROM tbl_word")
    fun getWord(): List<WordModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wordModel: WordModel)

    @Delete
    fun delete(wordModel: WordModel)
}