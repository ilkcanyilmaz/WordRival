package com.ilkcanyilmaz.wordrival.di

import android.content.Context
import androidx.room.Room
import com.ilkcanyilmaz.wordrival.daos.FriendDao
import com.ilkcanyilmaz.wordrival.daos.QuestionDao
import com.ilkcanyilmaz.wordrival.daos.UserDao
import com.ilkcanyilmaz.wordrival.daos.WordDao
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.repositories.FirestoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): DatabaseManager {
        var INSTANCE: DatabaseManager? = null

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                appContext.applicationContext,
                DatabaseManager::class.java,
                "database-WordRival"
            ).allowMainThreadQueries()
                .build()
        }
        return INSTANCE
    }

    @Singleton
    @Provides
    fun provideUserDao(database: DatabaseManager): UserDao {
        return database.userDao()
    }

    @Singleton
    @Provides
    fun provideFriendDao(database: DatabaseManager): FriendDao {
        return database.friendDao()
    }

    @Singleton
    @Provides
    fun provideQuestionDao(database: DatabaseManager): QuestionDao {
        return database.questionDao()
    }

    @Singleton
    @Provides
    fun provideWordDao(database: DatabaseManager): WordDao {
        return database.wordDao()
    }

    @Singleton
    @Provides
    fun provideFirestoreRepository(@ApplicationContext appContext: Context): FirestoreRepository {
        val repo= FirestoreRepository(appContext)
        return repo
    }
}
