package com.libreria.androidproject.ui.activity.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Order
import java.text.SimpleDateFormat
import java.util.*


class OrderAdapter(
    private val items: MutableList<Order> = mutableListOf(),
    private var titleMap: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<OrderAdapter.VH>() {

    fun update(newItems: List<Order>, newTitles: Map<String, String>) {
        items.clear()
        items.addAll(newItems)
        titleMap = newTitles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvOrderId = view.findViewById<TextView>(R.id.tvOrderId)
        private val tvOrderDate = view.findViewById<TextView>(R.id.tvOrderDate)

        fun bind(order: Order) {
            val title = titleMap[order.libroId] ?: "Pedido: ${order.id}"
            tvOrderId.text = "Libro: $title"
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvOrderDate.text = fmt.format(order.timestamp.toDate())
        }
    }
}