package com.app.daily.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.app.daily.R
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.domain.models.ItemModel
import com.app.daily.domain.models.ListModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditItemDialog : DialogFragment() {

    @Inject
    lateinit var listsRepositoryImpl: ListsRepositoryImpl

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            var name = arguments?.getString("name")
            val listId = arguments?.getString("listId")
            val itemId = arguments?.getString("itemId")

            val addView: View = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_list, view as ViewGroup?, false)

            val inputName = addView.findViewById<TextInputLayout>(R.id.inputSetName)

            inputName.editText?.setText(name)

            inputName.editText?.requestFocus()

            inputName.editText?.doAfterTextChanged { editable ->
                name = editable.toString()
            }

            val dialog = MaterialAlertDialogBuilder(it).setView(addView).setTitle("Edit Item")
                .setCancelable(false).setPositiveButton("Done", null)
                .setNegativeButton("Cancel") { _, _ ->
                    dialog?.dismiss()
                }.create()

            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                inputName.error = if (name!!.isBlank()) {
                    "List name can't be empty"
                } else {
                    null
                }
                if (inputName.error == null) {
                    serviceScope.launch {
                        val list = listsRepositoryImpl.getList(listId!!)
                        if (list != null) {
                            val updatedContent = list.content.map { item ->
                                if(item.id == itemId) {
                                    ItemModel(
                                        item.id,
                                        name!!,
                                        item.checked
                                    )
                                } else{
                                    item
                                }
                            }
                            listsRepositoryImpl.updateList(
                                ListModel(
                                    list.id, list.name, list.timestamp, list.priority, updatedContent
                                )
                            )
                        }

                    }.invokeOnCompletion {
                        dialog.dismiss()
                    }
                }
            }

            return dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "EditItemDialog"
    }
}