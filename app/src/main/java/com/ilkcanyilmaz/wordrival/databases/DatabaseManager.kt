package com.ilkcanyilmaz.wordrival.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ilkcanyilmaz.wordrival.daos.FriendDao
import com.ilkcanyilmaz.wordrival.daos.QuestionDao
import com.ilkcanyilmaz.wordrival.daos.UserDao
import com.ilkcanyilmaz.wordrival.daos.WordDao
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.models.WordModel

@Database(entities = [User::class, Friend::class, Question::class, WordModel::class], version = 1, exportSchema = false)
abstract class DatabaseManager : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao
    abstract fun questionDao(): QuestionDao
    abstract fun wordDao(): WordDao

}