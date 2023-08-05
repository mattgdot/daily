package com.app.daily.data.repository

import androidx.lifecycle.MutableLiveData
import com.app.daily.domain.models.UserModel
import com.app.daily.domain.repositories.UsersRepository
import com.google.firebase.firestore.*

import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    @Named("users_ref")
    private val usersRef: CollectionReference
) : UsersRepository {

    override suspend fun getUser(userId: String): UserModel? {
        return try {
            val documentSnapshot = usersRef.document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(UserModel::class.java)
                user
            } else {
                null
            }
        } catch (exception: Exception) {
            null
        }
    }

    override fun listenToUserListUpdates(userId: String): MutableLiveData<DocumentSnapshot> {
        val userDocumentData: MutableLiveData<DocumentSnapshot> = MutableLiveData()

        usersRef.document(userId).addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                userDocumentData.value = null
                return@EventListener
            }

            if (value != null && value.exists()) {
                userDocumentData.value=value
            } else {
                userDocumentData.value=null
            }
        })

        return userDocumentData
    }


    override suspend fun updateUser(user: UserModel) {
        usersRef.document(user.id).set(user, SetOptions.merge())
    }

    override suspend fun addUser(userId: String, name: String, lists: MutableList<String>) {
        val user = UserModel(
            id = userId, name = name, lists = lists
        )
        usersRef.document(userId).set(user)
    }

    override suspend fun deleteUser(userId: String) {
        try {
            usersRef.document(userId).delete()
        } catch (_: Exception) {

        }
    }
}