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
        val txtDescripcion = findViewById<EditText>(R.id.txtDescripcion)
        val txtFchPublicacion = findViewById<EditText>(R.id.txtFchaPublica)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtStock = findViewById<EditText>(R.id.txtStock)
        val txtAutor = findViewById<EditText>(R.id.txtAutor)
        val txtPortada = findViewById<EditText>(R.id.txtPortada)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val btnIrListado = findViewById<Button>(R.id.btnListado)
        btnRegistrar.setOnClickListener {
            val titulo = txtTitulo.text.toString()
            val descripcion = txtDescripcion.text.toString()
            val fchapublicacion = txtFchPublicacion.text.toString()
            val precio = txtPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = txtStock.text.toString().toIntOrNull() ?: 0
            val autor = txtAutor.text.toString()
            val portada = txtPortada.text.toString()
            val libro = Libro(titulo = titulo, descripcion = descripcion, fchpub = fchapublicacion,
                precio = precio, stock = stock, autor = autor, portada = portada)
            dbHelper.insertarLibro(libro)
            txtTitulo.text.clear()
            txtDescripcion.text.clear()
            txtFchPublicacion.text.clear()
            txtPrecio.text.clear()
            txtStock.text.clear()
            txtAutor.text.clear()
            txtPortada.text.clear()
        }

        btnIrListado.setOnClickListener {
            startActivity(Intent(this, ListaLibrosActivity::class.java))
        }

    }

}