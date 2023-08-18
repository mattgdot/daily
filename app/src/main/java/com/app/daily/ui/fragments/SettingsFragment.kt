package com.app.daily.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.daily.R
import com.app.daily.databinding.FragmentSettingsBinding
import com.app.daily.ui.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class SettingsFragment : DialogFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var themeDialog: AlertDialog
    private lateinit var changelogDialog: AlertDialog
    private lateinit var localeDialog: AlertDialog

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_App_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val themeModes = arrayOf("System default", "Light", "Dark")

        val availableLocales = Locale.getAvailableLocales()

        val localeNames = availableLocales.map { it.displayName }.toTypedArray()

        var checkedItem =
            availableLocales.indexOfFirst { it.toString() == mainViewModel.voiceLocale }

        binding.languageItem.supportText.text = localeNames[checkedItem]

        binding.modeItem.supportText.text = themeModes[mainViewModel.selectedThemeMode]

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.changelogItem.setOnClickListener {
            changelogDialog =
                MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.title_update))
                    .setMessage(resources.getString(R.string.message_update))
                    .setNegativeButton(resources.getString(R.string.btn_ok)) { _, _ ->
                    }.create()
            changelogDialog.show()
        }

        binding.languageItem.setOnClickListener {

            checkedItem =
                availableLocales.indexOfFirst { it.toString() == mainViewModel.voiceLocale }
            var selectedItem = checkedItem

            localeDialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Choose language")
                .setSingleChoiceItems(
                    localeNames, checkedItem
                ) { _, which ->
                    selectedItem = which
                }.setNegativeButton("Cancel") { _, _ ->
                }.setPositiveButton("Done") { _, _ ->
                    if (selectedItem != checkedItem) {
                        mainViewModel.setLocale(availableLocales[selectedItem].toString())
                        binding.languageItem.supportText.text = localeNames[selectedItem]
                    }
                }.create()
            localeDialog.show()

        }

        binding.modeItem.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle("Theme mode")

            val checkedItem = mainViewModel.selectedThemeMode
            var selectedItem = checkedItem

            builder.setSingleChoiceItems(
                themeModes, checkedItem
            ) { _, which ->
                selectedItem = which
            }

            builder.setPositiveButton("OK") { _, _ ->
                mainViewModel.setTheme(selectedItem)
                binding.modeItem.supportText.text = themeModes[mainViewModel.selectedThemeMode]
            }
            builder.setNegativeButton("Cancel", null)

            themeDialog = builder.create()
            themeDialog.show()
            true
        }

        return binding.root

    }

    companion object {
        const val TAG = "SettingsDialog"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            themeDialog.dismiss()
        } catch (e: Exception) {

        }
        try {
            changelogDialog.dismiss()
        } catch (e: Exception) {

        }
        try {
            localeDialog.dismiss()
        } catch (e: Exception) {

        }
        _binding = null
    }
}