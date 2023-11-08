package ru.assist.demo.ui.storage

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.assist.sdk.models.AssistResult

class OrderAdapter(
    private val onClick: (AssistResult) -> Unit
) : RecyclerView.Adapter<OrderViewHolder>() {

    private val items = mutableListOf<AssistResult>()

    fun setItems(newItems: List<AssistResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) =
        items[position]

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(parent = parent, onClick = onClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
