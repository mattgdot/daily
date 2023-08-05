package com.app.daily.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.ui.viewmodels.ListsFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeleteListDialog : DialogFragment() {

    @Inject
    lateinit var usersRepositoryImpl: UsersRepositoryImpl

    @Inject
    lateinit var sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProvider(this)[ListsFragmentViewModel::class.java]

        return activity?.let {

            val id = arguments?.getString("id")

            val dialog = MaterialAlertDialogBuilder(it).setTitle("Delete List?")
                .setMessage("This action can not be undone.").setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {

                        val user = usersRepositoryImpl.getUser(
                            viewModel.userID
                        )

                        if (user != null) {
                            user.lists.remove(id)
                            usersRepositoryImpl.updateUser(user)
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
        const val TAG = "DeleteListDialog"
    }
}