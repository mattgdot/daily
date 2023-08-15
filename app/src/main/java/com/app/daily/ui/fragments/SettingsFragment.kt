package com.app.daily.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.app.daily.R
import com.app.daily.databinding.FragmentSettingsBinding

class SettingsFragment : DialogFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_App_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        return binding.root

    }

    companion object {
        const val TAG = "SettingsDialog"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}