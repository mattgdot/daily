package com.app.daily.ui.adapters

import android.content.Context
import android.view.*
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.daily.R
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.databinding.ItemListBinding
import com.app.daily.domain.models.ListModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class ListsAdapter(private val startDragListener: StartDragListener) : ListAdapter<ListModel, ListsAdapter.RadioStationViewHolder>(
    object : DiffUtil.ItemCallback<ListModel>() {
        override fun areItemsTheSame(oldItem: ListModel, newItem: ListModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ListModel, newItem: ListModel): Boolean =
            oldItem.name == newItem.name
    },
), ItemMoveCallback.ItemTouchHelperContract {

    var onItemClick: ((ListModel) -> Unit)? = null
    var onOptionsPressed: ((ListModel, View, Int) -> Unit)? = null
    var onOrderChanged: (() -> Unit)? = null


    class RadioStationViewHolder(binding: ItemListBinding) :

        RecyclerView.ViewHolder(binding.root) {
            val listName = binding.tvListName
            val listDate = binding.tvListDate
            val listOptions = binding.ibOptions
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioStationViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context))

        return RadioStationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioStationViewHolder, position: Int) {
        holder.listName.text = getItem(holder.adapterPosition).name

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        holder.listDate.text = dateFormat.format(Date(getItem(holder.adapterPosition).timestamp))

        holder.listOptions.setOnClickListener {
            onOptionsPressed?.invoke(getItem(holder.adapterPosition),it, R.menu.options_menu)
        }

        holder.itemView.setOnLongClickListener {
            startDragListener.requestDrag(holder)
            false
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(currentList.toMutableList(), i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(currentList.toMutableList(), i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: MyListsAdapter.ListsViewHolder) {
    }

    override fun onRowClear(myViewHolder: MyListsAdapter.ListsViewHolder) {
        onOrderChanged?.invoke()
    }
}