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
    private val items: List<Order>
) : RecyclerView.Adapter<OrderAdapter.VH>() {

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
        private val tvOrderId   = view.findViewById<TextView>(R.id.tvOrderId)
        private val tvOrderDate = view.findViewById<TextView>(R.id.tvOrderDate)

        fun bind(o: Order) {
            tvOrderId.text = "Pedido: ${o.id}"
            val date = o.timestamp.toDate()
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvOrderDate.text = fmt.format(date)
        }
    }
}