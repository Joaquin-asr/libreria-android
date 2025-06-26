package com.libreria.androidproject

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.libreria.androidproject.R

class LibroAdapter(
    ctx: Context,
    libros: List<Libro>
) : ArrayAdapter<Libro>(ctx, 0, libros) {

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_libro, parent, false)
        val libro = getItem(pos)!!

        val imgPortada = view.findViewById<ImageView>(R.id.imgPortada)
        val tvTitulo   = view.findViewById<TextView>(R.id.tvTitulo)
        val tvDesc     = view.findViewById<TextView>(R.id.tvDescripcion)
        val tvAutor    = view.findViewById<TextView>(R.id.tvAutor)
        val tvFecha    = view.findViewById<TextView>(R.id.tvFecha)
        val tvPrecio   = view.findViewById<TextView>(R.id.tvPrecioStock)

        tvTitulo.text      = libro.titulo
        tvDesc.text        = libro.descripcion
        tvAutor.text       = "Autor: ${libro.autor}"
        val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        tvFecha.text       = fmt.format(java.util.Date(libro.fchpub))
        tvPrecio.text      = "S/.${"%.2f".format(libro.precio)}  •  Stock: ${libro.stock}"

        if (libro.portadaUri.isNotEmpty()) {
            try {
                imgPortada.setImageURI(Uri.parse(libro.portadaUri))
            } catch (e: SecurityException) {
                imgPortada.setImageResource(R.drawable.ic_book)
            }
        } else {
            imgPortada.setImageResource(R.drawable.ic_book)
        }

        return view
    }
}