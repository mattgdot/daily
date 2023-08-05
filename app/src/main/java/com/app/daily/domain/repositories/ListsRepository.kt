package com.app.daily.domain.repositories

import androidx.lifecycle.MutableLiveData
import com.app.daily.domain.models.ItemModel
import com.app.daily.domain.models.ListModel

interface ListsRepository {
    suspend fun getList(listId: String): ListModel?

    suspend fun addList(name: String, priority:Int, content: List<ItemModel>): String

    fun listenToListChanges(listIds: List<String>):MutableLiveData<MutableList<ListModel>>

    suspend fun updateList(list:ListModel)

    suspend fun deleteList(listId: String)
}