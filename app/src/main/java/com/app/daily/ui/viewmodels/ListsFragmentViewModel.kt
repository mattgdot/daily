package com.app.daily.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.domain.models.ListModel
import com.app.daily.domain.models.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListsFragmentViewModel @Inject constructor(
    private val usersRepositoryImpl: UsersRepositoryImpl,
    private val listsRepositoryImpl: ListsRepositoryImpl,
    sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl
) : ViewModel() {

    private var _userLists: MutableLiveData<DocumentSnapshot> = MutableLiveData()
    val userLists: LiveData<DocumentSnapshot>
        get() = _userLists

    var userID: String = sharedPreferencesRepositoryImpl.getUserId().toString()

    fun setUserLists(lists: List<String>): MutableLiveData<MutableList<ListModel>> {
        return listsRepositoryImpl.listenToListChanges(lists)
    }

    suspend fun updateUserLists(newList: List<ListModel>) {
        val currentUser = usersRepositoryImpl.getUser(
            userID
        )
        if (currentUser != null) {
            usersRepositoryImpl.updateUser(
                UserModel(
                    id = currentUser.id,
                    name = currentUser.name,
                    lists = newList.reversed().map {
                        it.id
                    }.toMutableList()
                )
            )
        }
    }

    init {
        if (userID.isNotEmpty()) {
            _userLists = usersRepositoryImpl.listenToUserListUpdates(userID)
        }
    }

}