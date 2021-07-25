package com.ilkcanyilmaz.wordrival.repositories

import android.os.Handler
import android.os.Looper
import com.ilkcanyilmaz.wordrival.daos.UserDao
import com.ilkcanyilmaz.wordrival.models.User
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

class UserLocalDataSource @Inject constructor(private val userDao: UserDao) : UserDataSource {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun addUser(user: User) {
        executorService.execute {
            userDao.insert(
                user
            )
        }
    }

    override fun getUser(callback: (User?) -> Unit) {
        executorService.execute {
            val user = userDao.getUser()
            mainThreadHandler.post { callback(user) }
        }
    }

    override fun deleteUser(user: User) {
        executorService.execute {
            userDao.delete(user)
        }
    }
}