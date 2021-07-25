package com.ilkcanyilmaz.wordrival.repositories

import com.ilkcanyilmaz.wordrival.models.User

interface UserDataSource {
    fun addUser(user: User)
    fun getUser(callback: (User?) -> Unit)
    fun deleteUser(user: User)
}