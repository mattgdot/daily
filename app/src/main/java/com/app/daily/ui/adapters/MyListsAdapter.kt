package com.app.daily.ui.adapters


import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.daily.R
import com.app.daily.databinding.ItemListBinding
import com.app.daily.domain.models.ListModel
import com.app.daily.utils.DiffCallback
import java.text.SimpleDateFormat
import java.util.*


class MyListsAdapter (val list: ArrayList<ListModel>, private val startDragListener: StartDragListener) : RecyclerView.Adapter<MyListsAdapter.ListsViewHolder>(),
    ItemMoveCallback.ItemTouchHelperContract{

    private val diffCallback = DiffCallback(list, ArrayList())

    fun submitList(updatedList: List<ListModel>) {
        diffCallback.newList = updatedList
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        list.clear()
        list.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    //var onItemClick: ((ListModel) -> Unit)? = null
    var onOptionsPressed: ((ListModel, View, Int) -> Unit)? = null
    var onOrderChanged: (() -> Unit)? = null

    class ListsViewHolder(binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
            val listName = binding.tvListName
            val listDate = binding.tvListDate
            val listOptions = binding.ibOptions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListsViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context))

        return ListsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ListsViewHolder, position: Int) {
        holder.listName.text = list[holder.adapterPosition].name

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        holder.listDate.text = dateFormat.format(Date(list[holder.adapterPosition].timestamp))

        holder.itemView.setOnLongClickListener {
            startDragListener.requestDrag(holder)
            false
        }

        holder.listOptions.setOnClickListener {
            onOptionsPressed?.invoke(list[holder.adapterPosition],it,R.menu.options_menu)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ListsViewHolder) {
    }

    override fun onRowClear(myViewHolder: ListsViewHolder) {
        onOrderChanged?.invoke()
    }

}