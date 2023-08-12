package com.app.daily.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.daily.R
import com.app.daily.databinding.FragmentItemsBinding
import com.app.daily.domain.models.ItemModel
import com.app.daily.ui.adapters.ItemsAdapter
import com.app.daily.ui.dialogs.*
import com.app.daily.ui.viewmodels.ListsFragmentViewModel
import com.app.daily.utils.ItemMoveCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class ItemsFragment : DialogFragment() {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy { ItemsAdapter(arrayListOf()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_App_FullScreenDialog)
    }

    private fun showMenu(v: View, menuRes: Int, item: ItemModel, listId:String) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {

                    val bundle = Bundle().apply {
                        putString("listId", listId)
                        putString("name", item.name)
                        putString("itemId", item.id)
                    }

                    EditItemDialog().apply {
                        arguments = bundle
                    }.also {
                        it.show(requireActivity().supportFragmentManager, EditItemDialog.TAG)
                    }
                }
                R.id.delete -> {
                    val bundle = Bundle().apply {
                        putString("listId", listId)
                        putString("itemId", item.id)
                    }

                    DeleteItemDialog().apply {
                        arguments = bundle
                    }.also {
                        it.show(
                            requireActivity().supportFragmentManager, DeleteItemDialog.TAG
                        )
                    }
                    popup.dismiss()
                }
            }
            true
        }
        popup.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentItemsBinding.inflate(inflater, container, false)

        val viewModel = ViewModelProvider(this)[ListsFragmentViewModel::class.java]

        val id = arguments?.getString("id").toString()
        val name = arguments?.getString("name")

        binding.toolbar.setNavigationOnClickListener {
            viewModel.dialogShown=false
            dismiss()
        }

        val speech = SpeechRecognizer.createSpeechRecognizer(requireContext())

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.clean_list -> {
                    val bundle = Bundle().apply {
                        putString("id", id)
                    }

                    DeleteAllItemsDialog().apply {
                        arguments = bundle
                    }.also {
                        it.show(
                            requireActivity().supportFragmentManager, DeleteAllItemsDialog.TAG
                        )
                    }
                }
                R.id.list_share -> {
                    Log.d("mkv","share")
                }
                R.id.check_all -> {
                    lifecycleScope.launch {
                        val checked = adapter.list.toMutableList().map { item ->
                            ItemModel(
                                item.id,
                                item.name,
                                true
                            )
                        }
                        viewModel.updateItemsLists(checked,id)
                    }
                }
                R.id.uncheck_all -> {
                    lifecycleScope.launch {
                        val unChecked = adapter.list.toMutableList().map { item ->
                            ItemModel(
                                item.id,
                                item.name,
                                false
                            )
                        }
                        viewModel.updateItemsLists(unChecked,id)
                    }
                }
                R.id.sort_all -> {
                    lifecycleScope.launch {
                        val (uncheckedItems, checkedItems) = adapter.list.partition { !it.checked }
                        val sorted = uncheckedItems.toMutableList()
                        sorted.sortBy {
                            it.name
                        }
                        viewModel.updateItemsLists(sorted+checkedItems,id)
                    }
                }
                R.id.delete_checked -> {
                    val (uncheckedItems, checkedItems) = adapter.list.partition { !it.checked }
                    lifecycleScope.launch {
                        viewModel.updateItemsLists(uncheckedItems,id)
                    }
                }
            }
            true
        }

        binding.toolbar.title = name

        binding.rvListItems.adapter = adapter

        ItemTouchHelper(
            ItemMoveCallback(adapter)
        ).also {
            it.attachToRecyclerView(binding.rvListItems)
        }

        viewModel.listenToItems(id).observe(viewLifecycleOwner) { curList  ->
            curList?.let { list ->
                binding.pbLoading.isVisible=false

                val items = list.content
                val previousItemCount = adapter.itemCount
                if (items.isEmpty()) {
                    adapter.submitList(emptyList())
                    binding.tvNoItems.visibility = View.VISIBLE
                } else {
                    val (uncheckedItems, checkedItems) = items.partition { !it.checked }
                    val sortedItems = uncheckedItems + checkedItems

                    adapter.submitList(sortedItems)

                    binding.tvNoItems.visibility = View.GONE
                    if (previousItemCount < items.size) {
                        binding.rvListItems.post {
                            binding.rvListItems.scrollToPosition(0)
                        }
                    }
                }
            } ?: run {
                adapter.submitList(emptyList())
                binding.tvNoItems.visibility = View.VISIBLE
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            // Prevent the list from scrolling when an item position change
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                lifecycleScope.launch {
                    binding.rvListItems.scrollToPosition(fromPosition)
                }
            }
        })

        adapter.onItemChecked = { item, isChecked ->
            val itemIndex = adapter.list.indexOf(item)
            val newItem = ItemModel(
                item.id,
                item.name,
                isChecked
            )
            val list = adapter.list
            list[itemIndex] = newItem
            lifecycleScope.launch {
                viewModel.updateItemsLists(list, id)
            }
        }

        adapter.onOrderChanged = {
            lifecycleScope.launch {
                viewModel.updateItemsLists(adapter.list.toMutableList(), id)
            }
        }

        adapter.onOptionsPressed = { list, view, menu ->
            showMenu(view, menu, list, id)
        }

        binding.btnAdd.setIconResource(R.drawable.ic_mic)

        binding.etItem.doOnTextChanged { text, start, before, count ->
            if (text != null) {
                if(text.isNotBlank()){
                    binding.btnAdd.setIconResource(R.drawable.ic_add)
                } else {
                    binding.btnAdd.setIconResource(R.drawable.ic_mic)
                }
            }
        }

        binding.etItem.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnAdd.performClick()
                return@OnEditorActionListener true
            }
            false
        })


        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if(data!=null){
                    val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if(text != null){
                        viewModel.addItemToList(
                            ItemModel(
                                UUID.randomUUID().toString(),
                                text[0],
                                false
                            ),
                            id
                        )
                    }
                }
            }
        }

        binding.btnAdd.setOnClickListener {
            val text = binding.etItem.text
            if (text.isNotBlank()) {
                binding.etItem.setText("")
                viewModel.addItemToList(
                    ItemModel(
                        UUID.randomUUID().toString(),
                        text.toString(),
                        false
                    ),
                    id
                )
            } else{
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

                try {
                    resultLauncher.launch(intent)
                } catch (e:Exception){
                    Snackbar.make(binding.root,"Can't start Google speech recognition", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root

    }

    companion object {
        const val TAG = "ItemsListDialog"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}