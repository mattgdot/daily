package com.app.daily.ui.viewmodels

import android.app.LauncherActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.domain.models.ItemModel
import com.app.daily.domain.models.ListModel
import com.app.daily.domain.models.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    var dialogShown = false

    fun setUserLists(lists: List<String>): MutableLiveData<MutableList<ListModel>> {
        return listsRepositoryImpl.listenToListChanges(lists)
    }
    fun listenToItems(listId: String): MutableLiveData<ListModel> {
        return listsRepositoryImpl.listenToItemsChanges(listId)
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
    suspend fun updateItemsLists(newList: List<ItemModel>, listId: String) {
        val currentUser = usersRepositoryImpl.getUser(
            userID
        )
        if (currentUser != null) {
            val list = listsRepositoryImpl.getList(listId)
            if (list != null) {
                listsRepositoryImpl.updateList(
                    ListModel(
                        list.id, list.name, list.timestamp, list.priority, newList
                    )
                )
            }
        }
    }

    fun addItemToList(item: ItemModel, listId:String){
        viewModelScope.launch {
            val user = usersRepositoryImpl.getUser(
                userID
            )
            if(user!=null){
                val list = listsRepositoryImpl.getList(listId)
                if (list != null) {
                    val updatedContent = list.content.toMutableList()
                    updatedContent.add(0,item)
                    listsRepositoryImpl.updateList(
                        ListModel(
                            list.id, list.name, list.timestamp, list.priority, updatedContent
                        )
                    )
                }
            }
        }
    }

    init {
        if (userID.isNotEmpty()) {
            _userLists = usersRepositoryImpl.listenToUserListUpdates(userID)
        }
    }

}