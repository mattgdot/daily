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
import androidx.lifecycle.lifecycleScope
import com.app.daily.R
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddListDialog : DialogFragment() {

    @Inject
    lateinit var listsRepositoryImpl: ListsRepositoryImpl

    @Inject
    lateinit var usersRepositoryImpl: UsersRepositoryImpl

    @Inject
    lateinit var sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val addView: View = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_list, view as ViewGroup?, false)

            val inputName = addView.findViewById<TextInputLayout>(R.id.inputSetName)

            inputName.editText?.requestFocus()

            var name = ""

            inputName.editText?.doAfterTextChanged { editable ->
                name = editable.toString()
            }

            val dialog = MaterialAlertDialogBuilder(it)
                .setView(addView)
                .setTitle("New List")
                .setCancelable(false)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel") { _, _ ->
                    dialog?.dismiss()
                }
                .create()

            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                inputName.error = if(name.isBlank()){
                    "List name can't be empty"
                }
                else {
                    null
                }
                if(inputName.error == null) {
                    lifecycleScope.launch {
                        val user = usersRepositoryImpl.getUser(
                            sharedPreferencesRepositoryImpl.getUserId().toString()
                        )

                        if(user != null){
                            val lastPriority = user.lists.size
                            val id = listsRepositoryImpl.addList(
                                name = name,
                                priority = lastPriority,
                                content = listOf()
                            )
                            user.lists.add(id)
                            usersRepositoryImpl.updateUser(user)
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
        const val TAG = "AddListDialog"
    }
}