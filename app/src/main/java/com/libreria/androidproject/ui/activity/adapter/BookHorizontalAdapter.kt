package com.libreria.androidproject.ui.activity.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Libro
import com.libreria.androidproject.ui.activity.UserDashboardActivity

class BookHorizontalAdapter(
    private val items: List<Libro>,
    private val click: (Libro) -> Unit
) : RecyclerView.Adapter<BookHorizontalAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_horizontal, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.imgBook)
        private val tv = view.findViewById<TextView>(R.id.tvBookTitle)

        fun bind(l: Libro) {
            tv.text = l.titulo
            if (l.portadaUri.isNotEmpty()) img.setImageURI(Uri.parse(l.portadaUri))
            else img.setImageResource(R.drawable.ic_book)

            view.setOnClickListener {
                (view.context as? UserDashboardActivity)
                    ?.showBookDetail(l.cod)
            }
        }
    }
}