package com.ilkcanyilmaz.wordrival.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ilkcanyilmaz.wordrival.daos.FriendDao
import com.ilkcanyilmaz.wordrival.daos.UserDao
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.User

@Database(entities = [User::class, Friend::class], version = 1)
abstract class DatabaseManager : RoomDatabase() {


    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao

    companion object {
        var INSTANCE: DatabaseManager? = null

        fun getDatabaseManager(context: Context): DatabaseManager? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseManager::class.java,
                    "database-WordRival"
                ).allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }

    }
}