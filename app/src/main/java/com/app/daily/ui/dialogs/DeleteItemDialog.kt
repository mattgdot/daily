package com.app.daily.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.domain.models.ListModel
import com.app.daily.ui.viewmodels.ListsFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeleteItemDialog : DialogFragment() {

    @Inject
    lateinit var listsRepositoryImpl: ListsRepositoryImpl

    @Inject
    lateinit var sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {

            val listId = arguments?.getString("listId")
            val itemId = arguments?.getString("itemId")

            val dialog = MaterialAlertDialogBuilder(it).setTitle("Delete Item?")
                .setMessage("This action can not be undone.").setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {

                        val list = listsRepositoryImpl.getList(listId!!)
                        if (list != null) {
                            val updatedContent = list.content.toMutableList().filter {item ->
                                item.id != itemId
                            }
                            listsRepositoryImpl.updateList(
                                ListModel(
                                    list.id, list.name, list.timestamp, list.priority, updatedContent
                                )
                            )
                        }

                    }.invokeOnCompletion {
                        dialog!!.dismiss()
                    }
                }
                .setNegativeButton("No") { _, _ ->
                }
                .create()

            dialog.show()

            return dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "DeleteItemsDialog"
    }
}