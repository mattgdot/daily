package com.app.daily.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.daily.R
import com.app.daily.databinding.FragmentListsBinding
import com.app.daily.domain.models.ListModel
import com.app.daily.domain.models.UserModel
import com.app.daily.utils.ItemMoveCallback
import com.app.daily.ui.adapters.ListsAdapter
import com.app.daily.ui.dialogs.AddListDialog
import com.app.daily.ui.dialogs.DeleteListDialog
import com.app.daily.ui.dialogs.EditListDialog
import com.app.daily.ui.viewmodels.ListsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ListsFragment : Fragment() {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        ListsAdapter(
            arrayListOf()
        )
    }

    private fun showMenu(v: View, menuRes: Int, list: ListModel) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    val bundle = Bundle().apply {
                        putString("name", list.name)
                        putString("id", list.id)
                    }

                    EditListDialog().apply {
                        arguments = bundle
                    }.also {
                        it.show(requireActivity().supportFragmentManager, EditListDialog.TAG)
                    }
                }
                R.id.delete -> {
                    val bundle = Bundle().apply {
                        putString("id", list.id)
                    }

                    DeleteListDialog().apply {
                        arguments = bundle
                    }.also {
                        it.show(
                            requireActivity().supportFragmentManager, DeleteListDialog.TAG
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

        _binding = FragmentListsBinding.inflate(inflater, container, false)

        val viewModel = ViewModelProvider(this)[ListsFragmentViewModel::class.java]

        binding.rvLists.adapter = adapter

        ItemTouchHelper(
            ItemMoveCallback(adapter)
        ).also {
            it.attachToRecyclerView(binding.rvLists)
        }

        binding.fabAddList.setOnClickListener {
            val addListDialog = AddListDialog()

            addListDialog.show(
                requireActivity().supportFragmentManager, AddListDialog.TAG
            )
        }

        viewModel.userLists.observe(viewLifecycleOwner) { documentSnapshot ->
            documentSnapshot?.let { snapshot ->
                val user = snapshot.toObject(UserModel::class.java) ?: return@observe

                binding.pbLoading.isVisible=false

                if (user.lists.isEmpty()) {
                    adapter.submitList(emptyList())
                    binding.tvNoItems.visibility = View.VISIBLE
                } else {
                    binding.tvNoItems.visibility = View.GONE
                    val previousItemCount = adapter.itemCount

                    viewModel.setUserLists(user.lists).observe(viewLifecycleOwner) { userLists ->
                        userLists?.let {
                            adapter.submitList(it.reversed())

                            if (previousItemCount < userLists.size) {
                                binding.rvLists.post {
                                    binding.rvLists.scrollToPosition(0)
                                }
                            }
                        } ?: run {
                            adapter.submitList(emptyList())
                            binding.tvNoItems.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        adapter.onOptionsPressed = { list, view, menu ->
            showMenu(view, menu, list)
        }

        adapter.onOrderChanged = {
            lifecycleScope.launch {
                viewModel.updateUserLists(adapter.list.toMutableList())
            }
        }

        adapter.onItemClicked = { list ->
            val bundle = Bundle().apply {
                putString("name", list.name)
                putString("id", list.id)
            }
            val itemsFragment = ItemsFragment()
            //if(!viewModel.dialogShown) {
                itemsFragment.apply {
                    arguments = bundle
                } .show(requireActivity().supportFragmentManager, ItemsFragment.TAG)
                viewModel.dialogShown=true
            //}
        }

        binding.rvLists.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 10 && binding.fabAddList.isShown) {
                    binding.fabAddList.hide()
                }

                if (dy < -10 && !binding.fabAddList.isShown) {
                    binding.fabAddList.show()
                }

                if (!recyclerView.canScrollVertically(-1)) {
                    binding.fabAddList.show()
                }
            }
        })

        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}