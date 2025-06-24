package com.libreria.androidproject


import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: LibroDBHelper

    private lateinit var txtTitulo: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtFchPublicacion: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var txtStock: EditText
    private lateinit var txtAutor: EditText
    private lateinit var txtPortada: EditText

    private var fechaSeleccionada: Long = 0L
    private var uriPortada: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriPortada = it
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    txtPortada.setText(cursor.getString(idx))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = LibroDBHelper(this)

        txtTitulo = findViewById(R.id.txtTitulo)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtFchPublicacion = findViewById(R.id.txtFchPublicacion)
        txtPrecio = findViewById(R.id.txtPrecio)
        txtStock = findViewById(R.id.txtStock)
        txtAutor = findViewById(R.id.txtAutor)
        txtPortada = findViewById(R.id.txtPortada)

        txtFchPublicacion.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    fechaSeleccionada = cal.timeInMillis
                    txtFchPublicacion.setText(
                        java.text.SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(cal.time)
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        txtPortada.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val libro = Libro(
                titulo      = txtTitulo.text.toString(),
                descripcion = txtDescripcion.text.toString(),
                fchpub      = fechaSeleccionada,
                precio      = txtPrecio.text.toString().toDoubleOrNull() ?: 0.0,
                stock       = txtStock.text.toString().toIntOrNull() ?: 0,
                autor       = txtAutor.text.toString(),
                portadaUri  = uriPortada?.toString() ?: ""
            )
            dbHelper.insertarLibro(libro)
            clearFields()
        }

        findViewById<Button>(R.id.btnListado).setOnClickListener {
            startActivity(Intent(this, ListaLibrosActivity::class.java))
        }
    }

    private fun clearFields() {
        txtTitulo.text.clear()
        txtDescripcion.text.clear()
        txtFchPublicacion.text.clear()
        txtPrecio.text.clear()
        txtStock.text.clear()
        txtAutor.text.clear()
        txtPortada.text.clear()
        fechaSeleccionada = 0L
        uriPortada = null
    }
}