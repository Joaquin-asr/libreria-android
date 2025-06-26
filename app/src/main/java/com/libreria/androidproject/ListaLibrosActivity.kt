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
    private var currentPortadaEditText: EditText? = null

    private val pickDocForEditLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            uriTmp = it
            contentResolver.query(it, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) {
                    currentPortadaEditText?.setText(c.getString(idx))
                }
            }
        }
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
        fechaTmp = 0L
        uriTmp = null
        currentPortadaEditText = null

        val dlgView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_update_libro, null, false)

        val etT  = dlgView.findViewById<EditText>(R.id.etTitulo)
        val etD  = dlgView.findViewById<EditText>(R.id.etDescripcion)
        val etF  = dlgView.findViewById<EditText>(R.id.etFchPub)
        val etP  = dlgView.findViewById<EditText>(R.id.etPrecio)
        val etS  = dlgView.findViewById<EditText>(R.id.etStock)
        val etA  = dlgView.findViewById<EditText>(R.id.etAutor)
        val etPo = dlgView.findViewById<EditText>(R.id.etPortada)

        etT.setText(libro.titulo)
        etD.setText(libro.descripcion)
        etF.setText(
            java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(libro.fchpub))
        )
        etP.setText(libro.precio.toString())
        etS.setText(libro.stock.toString())
        etA.setText(libro.autor)
        if (libro.portadaUri.isNotEmpty()) {
            contentResolver.query(Uri.parse(libro.portadaUri), null, null, null, null)
                ?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst()) etPo.setText(c.getString(idx))
                }
            uriTmp = Uri.parse(libro.portadaUri)
        }

        etF.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = libro.fchpub }
            DatePickerDialog(this,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    fechaTmp = calendar.timeInMillis
                    etF.setText(
                        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etPo.setOnClickListener {
            currentPortadaEditText = etPo
            pickDocForEditLauncher.launch(arrayOf("image/*"))
        }

        val camposObligatorios = listOf(etT, etD, etF, etP, etS, etA)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Actualizar Libro")
            .setView(dlgView)
            .setNegativeButton("Eliminar") { _, _ ->
                dbHelper.eliminarLibro(libro.cod)
                cargarLista()
            }
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val todosValidos = camposObligatorios
                .map { it.validateRequired() }
                .all { it }
            if (!todosValidos) return@setOnClickListener

            libro.titulo      = etT.text.toString()
            libro.descripcion = etD.text.toString()
            libro.fchpub      = if (fechaTmp != 0L) fechaTmp else libro.fchpub
            libro.precio      = etP.text.toString().toDouble()
            libro.stock       = etS.text.toString().toInt()
            libro.autor       = etA.text.toString()
            libro.portadaUri  = uriTmp?.toString() ?: libro.portadaUri

            dbHelper.actualizarLibro(libro)
            cargarLista()
            dialog.dismiss()
        }
    }
}