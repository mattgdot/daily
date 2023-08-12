package com.app.daily.ui.adapters

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.daily.R
import com.app.daily.databinding.ItemItemBinding
import com.app.daily.domain.models.ItemModel
import com.app.daily.utils.ItemDiffCallback
import com.app.daily.utils.ItemMoveCallback
import com.google.type.Color
import java.util.*


class ItemsAdapter (val list: ArrayList<ItemModel>) : RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder>(), ItemMoveCallback.ItemTouchHelperContract{

    private val diffCallback = ItemDiffCallback(list, ArrayList())

    fun submitList(updatedList: List<ItemModel>) {
        diffCallback.newList = updatedList
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        list.clear()
        list.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    var onItemChecked: ((ItemModel, Boolean) -> Unit)? = null
    var onOptionsPressed: ((ItemModel, View, Int) -> Unit)? = null
    var onOrderChanged: (() -> Unit)? = null

    class ItemsViewHolder(binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val item = binding.item
        val itemOptions = binding.itemOptions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context))

        return ItemsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.item.text = list[holder.adapterPosition].name

        if (list[holder.adapterPosition].checked) {
            holder.item.isChecked=true
            holder.item.paintFlags = holder.item.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.item.isChecked=false
            holder.item.paintFlags = holder.item.paintFlags and (android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv())
        }

        holder.item.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.item.paintFlags = holder.item.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.item.paintFlags = holder.item.paintFlags and (android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv())
            }
            onItemChecked?.invoke(list[holder.adapterPosition], isChecked)
        }

        holder.itemOptions.setOnClickListener {
            onOptionsPressed?.invoke(list[holder.adapterPosition],it, R.menu.options_menu)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if(list[fromPosition].checked){
            return
        }
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

    override fun onRowClear() {
        onOrderChanged?.invoke()
    }

}