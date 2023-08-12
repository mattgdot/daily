package com.app.daily.data.repository

import androidx.lifecycle.MutableLiveData
import com.app.daily.domain.models.ItemModel
import com.app.daily.domain.models.ListModel
import com.app.daily.domain.repositories.ListsRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ListsRepositoryImpl @Inject constructor(
    @Named("lists_ref")
    private val listsRef: CollectionReference
) : ListsRepository {
    private var currentListener: ListenerRegistration? = null
    private var currentItemsListener: ListenerRegistration? = null

    override suspend fun getList(listId: String): ListModel? {
        return try {
            val documentSnapshot = listsRef.document(listId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(ListModel::class.java)
                user
            } else {
                null
            }
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun addList(name: String, priority: Int, content: List<ItemModel>): String {
        val id = listsRef.document().id
        val list = ListModel(
            id = id,
            name = name,
            priority = priority,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        listsRef.document(id).set(list)
        return id
    }

    override fun listenToListChanges(listIds: List<String>): MutableLiveData<MutableList<ListModel>> {
        val listsLiveData: MutableLiveData<MutableList<ListModel>> = MutableLiveData()

        currentListener?.remove()

        currentListener = listsRef.whereIn("id", listIds)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    listsLiveData.value=null
                    return@EventListener
                }

                if(value!=null){
                    val lists = mutableListOf<ListModel>()
                    for (doc in value) {
                        lists.add(doc.toObject(ListModel::class.java))
                    }
                    val orderById = listIds.withIndex().associate { (index, it) -> it to index }
                    val sortedPeople = lists.sortedBy { orderById[it.id] }
                    listsLiveData.postValue(sortedPeople.toMutableList())
                } else{
                    listsLiveData.value=null
                }
            })


        return listsLiveData
    }

    override fun listenToItemsChanges(listId: String): MutableLiveData<ListModel> {
        val listsLiveData: MutableLiveData<ListModel> = MutableLiveData()

        currentItemsListener?.remove()

        currentItemsListener = listsRef.document(listId)
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    listsLiveData.value=null
                    return@EventListener
                }

                if(value!=null){
                    listsLiveData.postValue(value.toObject(ListModel::class.java))
                } else{
                    listsLiveData.value=null
                }
            })


        return listsLiveData
    }

    override suspend fun updateList(list: ListModel) {
        listsRef.document(list.id).set(list, SetOptions.merge())
    }


    override suspend fun deleteList(listId: String) {
        try {
            listsRef.document(listId).delete()
        } catch (_: Exception) {

        }
    }
}