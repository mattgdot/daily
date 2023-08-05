package com.app.daily.domain.repositories

import androidx.lifecycle.MutableLiveData
import com.app.daily.domain.models.UserModel
import com.google.firebase.firestore.DocumentSnapshot

interface UsersRepository {
    suspend fun getUser(userId: String): UserModel?

    fun listenToUserListUpdates(userId: String): MutableLiveData<DocumentSnapshot>

    suspend fun updateUser(user: UserModel)

    suspend fun addUser(userId: String, name: String, lists: MutableList<String>)

    suspend fun deleteUser(userId: String)
}