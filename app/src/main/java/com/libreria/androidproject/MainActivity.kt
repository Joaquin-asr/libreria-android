package com.libreria.androidproject

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

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: LibroDBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var libros = mutableListOf<Libro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = LibroDBHelper(this)
        val txtTitulo = findViewById<EditText>(R.id.txtTitulo)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtStock = findViewById<EditText>(R.id.txtStock)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        listView = findViewById(R.id.lisLibros)
        btnRegistrar.setOnClickListener {
            val titulo = txtTitulo.text.toString()
            val precio = txtPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = txtStock.text.toString().toIntOrNull() ?: 0
            val libro = Libro(titulo = titulo, precio = precio, stock = stock)
            dbHelper.insertarLibro(libro)
            cargarLista()
            txtTitulo.text.clear()
            txtPrecio.text.clear()
            txtStock.text.clear()
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            val libro = libros[position]
            mostrarDialogoEditar(libro)
        }
        cargarLista()
    }
    private fun cargarLista() {
        libros = dbHelper.obtenerLibro().toMutableList()
        val titulos = libros.map { "${it.titulo} - \$${it.precio} - Stock:${it.stock}" }
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