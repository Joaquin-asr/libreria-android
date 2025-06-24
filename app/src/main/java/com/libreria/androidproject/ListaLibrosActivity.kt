package com.libreria.androidproject

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ListaLibrosActivity : AppCompatActivity() {

    private lateinit var dbHelper: LibroDBHelper
    private lateinit var listView: ListView
    private lateinit var btnNuevo: Button
    private var libros = mutableListOf<Libro>()

    private var fechaTmp: Long = 0L
    private var uriTmp: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uriTmp = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_libros)

        dbHelper = LibroDBHelper(this)
        listView = findViewById(R.id.lisLibros)
        btnNuevo = findViewById(R.id.btnNuevoLibro)

        listView.setOnItemClickListener { _, _, pos, _ ->
            mostrarDialogoEditar(libros[pos])
        }
        btnNuevo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        cargarLista()
    }

    private fun cargarLista() {
        libros = dbHelper.obtenerLibro().toMutableList()
        listView.adapter = LibroAdapter(this, libros)
    }

    private fun mostrarDialogoEditar(libro: Libro) {
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.dialog_update_libro, null, false)

        // EditText del diálogo
        val etT  = view.findViewById<EditText>(R.id.etTitulo)
        val etD  = view.findViewById<EditText>(R.id.etDescripcion)
        val etF  = view.findViewById<EditText>(R.id.etFchPub)
        val etP  = view.findViewById<EditText>(R.id.etPrecio)
        val etS  = view.findViewById<EditText>(R.id.etStock)
        val etA  = view.findViewById<EditText>(R.id.etAutor)
        val etPo = view.findViewById<EditText>(R.id.etPortada)

        // cargo valores
        etT.setText(libro.titulo)
        etD.setText(libro.descripcion)
        etF.setText(
            java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(libro.fchpub))
        )
        etP.setText(libro.precio.toString())
        etS.setText(libro.stock.toString())
        etA.setText(libro.autor)
        // mostramos solo nombre de archivo si existe
        if (libro.portadaUri.isNotEmpty()) {
            contentResolver.query(Uri.parse(libro.portadaUri), null, null, null, null)
                ?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst()) etPo.setText(c.getString(idx))
                }
            uriTmp = Uri.parse(libro.portadaUri)
        }

        // DatePicker en diálogo
        etF.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = libro.fchpub }
            DatePickerDialog(this,
                { _: DatePicker, y, m, d ->
                    cal.set(y, m, d)
                    fechaTmp = cal.timeInMillis
                    etF.setText(
                        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(cal.time)
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Imagen en diálogo
        etPo.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Actualizar Libro")
            .setView(view)
            .setPositiveButton("Guardar", DialogInterface.OnClickListener { _, _ ->
                libro.titulo      = etT.text.toString()
                libro.descripcion = etD.text.toString()
                libro.fchpub      = if (fechaTmp != 0L) fechaTmp else libro.fchpub
                libro.precio      = etP.text.toString().toDoubleOrNull() ?: 0.0
                libro.stock       = etS.text.toString().toIntOrNull() ?: 0
                libro.autor       = etA.text.toString()
                libro.portadaUri  = uriTmp?.toString() ?: libro.portadaUri
                dbHelper.actualizarLibro(libro)
                cargarLista()
                fechaTmp = 0L
                uriTmp = null
            })
            .setNegativeButton("Eliminar") { _, _ ->
                dbHelper.eliminarLibro(libro.cod)
                cargarLista()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }
}