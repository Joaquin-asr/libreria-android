package com.libreria.androidproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.libreria.androidproject.R

class LibroAdapter(
    context: Context,
    libros: List<Libro>
) : ArrayAdapter<Libro>(context, 0, libros) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // 1) Infla la vista si es null
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_libro, parent, false)

        // 2) Obtén el libro de esta posición
        val libro = getItem(position)!!

        // 3) Bind de datos
        val imgPortada = view.findViewById<ImageView>(R.id.imgPortada)
        val tvTitulo   = view.findViewById<TextView>(R.id.tvTitulo)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcion)
        val tvAutor    = view.findViewById<TextView>(R.id.tvAutor)
        val tvFecha    = view.findViewById<TextView>(R.id.tvFecha)
        val tvPrecioStock = view.findViewById<TextView>(R.id.tvPrecioStock)

        // Título y autor
        tvTitulo.text       = libro.titulo
        tvDescripcion.text  = libro.descripcion
        tvAutor.text        = libro.autor
        tvFecha.text        = libro.fchpub
        tvPrecioStock.text  = "S/.${libro.precio}  •  Stock: ${libro.stock}"

        // Placeholder de la portada (puedes cargar con Glide/Picasso si tienes URLs)
        imgPortada.setImageResource(R.drawable.ic_book)

        return view
    }
}