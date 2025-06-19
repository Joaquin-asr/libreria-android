package com.libreria.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.libreria.androidproject.R.id.lisLibros

class ListaLibrosActivity : AppCompatActivity() {
    private lateinit var dbHelper: LibroDBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var libros = mutableListOf<Libro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_libros)

        dbHelper = LibroDBHelper(this)
        listView = findViewById(lisLibros)
        val btnRegistrarNuevoLibro = findViewById<Button>(R.id.btnNuevoLibro)

        listView.setOnItemClickListener { _, _, position, _ ->
            val libro = libros[position]
            mostrarDialogoEditar(libro)
        }

        cargarLista()

        btnRegistrarNuevoLibro.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun cargarLista() {
        libros = dbHelper.obtenerLibro().toMutableList()
        val titulos = libros.map { "${it.titulo} - S/.${it.precio} - Stock:${it.stock}" }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titulos)
        listView.adapter = adapter
    }

    private fun mostrarDialogoEditar(libro: Libro) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val inputTitulo = EditText(this).apply { setText(libro.titulo) }
        val inputPrecio = EditText(this).apply { setText(libro.precio.toString()) }
        val inputStock = EditText(this).apply { setText(libro.stock.toString()) }

        layout.addView(inputTitulo)
        layout.addView(inputPrecio)
        layout.addView(inputStock)

        AlertDialog.Builder(this)
            .setTitle("Actualizar Libro")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                libro.titulo = inputTitulo.text.toString()
                libro.precio = inputPrecio.text.toString().toDoubleOrNull() ?: 0.0
                libro.stock = inputStock.text.toString().toIntOrNull() ?: 0
                dbHelper.actualizarLibro(libro)
                cargarLista()
            }
            .setNegativeButton("Eliminar") { _, _ ->
                dbHelper.eliminarLibro(libro.cod)
                cargarLista()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }
}