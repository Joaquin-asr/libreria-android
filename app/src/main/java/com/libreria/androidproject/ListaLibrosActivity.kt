package com.libreria.androidproject

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.view.View
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ListaLibrosActivity : AppCompatActivity() {

    private lateinit var dbHelper: LibroDBHelper
    private lateinit var listView: ListView
    private lateinit var btnRegistrarNuevo: Button
    private var libros = mutableListOf<Libro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_libros)

        dbHelper = LibroDBHelper(this)
        listView = findViewById(R.id.lisLibros)
        btnRegistrarNuevo = findViewById(R.id.btnNuevoLibro)

        listView.setOnItemClickListener { _, _, position, _ ->
            mostrarDialogoEditar(libros[position])
        }

        btnRegistrarNuevo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        cargarLista()
    }

    private fun cargarLista() {
        libros = dbHelper.obtenerLibro().toMutableList()
        listView.adapter = LibroAdapter(this, libros)
    }

    private fun mostrarDialogoEditar(libro: Libro) {
        val dlgView: View = LayoutInflater
            .from(this)
            .inflate(R.layout.dialog_update_libro, null, false)

        val etTitulo      = dlgView.findViewById<EditText>(R.id.etTitulo)
        val etDescripcion = dlgView.findViewById<EditText>(R.id.etDescripcion)
        val etFchPub      = dlgView.findViewById<EditText>(R.id.etFchPub)
        val etPrecio      = dlgView.findViewById<EditText>(R.id.etPrecio)
        val etStock       = dlgView.findViewById<EditText>(R.id.etStock)
        val etAutor       = dlgView.findViewById<EditText>(R.id.etAutor)
        val etPortada     = dlgView.findViewById<EditText>(R.id.etPortada)

        etTitulo.setText(libro.titulo)
        etDescripcion.setText(libro.descripcion)
        etFchPub.setText(libro.fchpub)
        etPrecio.setText(libro.precio.toString())
        etStock.setText(libro.stock.toString())
        etAutor.setText(libro.autor)
        etPortada.setText(libro.portada)

        AlertDialog.Builder(this)
            .setTitle("Actualizar Libro")
            .setView(dlgView)
            .setPositiveButton("Guardar", DialogInterface.OnClickListener { _, _ ->
                // Guarda cambios
                libro.titulo      = etTitulo.text.toString()
                libro.descripcion = etDescripcion.text.toString()
                libro.fchpub      = etFchPub.text.toString()
                libro.precio      = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                libro.stock       = etStock.text.toString().toIntOrNull() ?: 0
                libro.autor       = etAutor.text.toString()
                libro.portada     = etPortada.text.toString()
                dbHelper.actualizarLibro(libro)
                cargarLista()
            })
            .setNegativeButton("Eliminar", DialogInterface.OnClickListener { _, _ ->
                dbHelper.eliminarLibro(libro.cod)
                cargarLista()
            })
            .setNeutralButton("Cancelar", null)
            .show()
    }
}