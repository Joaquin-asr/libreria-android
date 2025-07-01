package com.libreria.androidproject


import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var txtTitulo: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtFchPublicacion: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var txtStock: EditText
    private lateinit var txtAutor: EditText
    private lateinit var txtPortada: EditText

    private var fechaSeleccionada: Long = 0L
    private var uriPortada: Uri? = null
    private var portadaNombreSeleccionada: String = ""

    private val pickDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uriPortada = it
            var nombreArchivo = ""
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    nombreArchivo = cursor.getString(idx)
                }
            }
            txtPortada.setText(nombreArchivo)
            portadaNombreSeleccionada = nombreArchivo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTitulo          = findViewById(R.id.txtTitulo)
        txtDescripcion     = findViewById(R.id.txtDescripcion)
        txtFchPublicacion  = findViewById(R.id.txtFchPublicacion)
        txtPrecio          = findViewById(R.id.txtPrecio)
        txtStock           = findViewById(R.id.txtStock)
        txtAutor           = findViewById(R.id.txtAutor)
        txtPortada         = findViewById(R.id.txtPortada)

        txtFchPublicacion.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    fechaSeleccionada = cal.timeInMillis
                    txtFchPublicacion.setText(
                        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(cal.time)
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        txtPortada.setOnClickListener {
            pickDocumentLauncher.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val campos = listOf(txtTitulo, txtDescripcion, txtFchPublicacion, txtPrecio, txtStock, txtAutor)
            val validos = campos.map { it.validateRequired() }.all { it }
            if (!validos) return@setOnClickListener

            val libro = Libro(
                titulo      = txtTitulo.text.toString(),
                descripcion = txtDescripcion.text.toString(),
                fchpub      = fechaSeleccionada,
                precio      = txtPrecio.text.toString().toDouble(),
                stock       = txtStock.text.toString().toInt(),
                autor       = txtAutor.text.toString(),
                portadaUri  = uriPortada?.toString() ?: "",
                portadaNombre  = portadaNombreSeleccionada

            )

            // aqui se inserta a la coleccion
            db.collection("libros")
                .add(libro.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, "Libro agregado (ID=${it.id})", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<Button>(R.id.btnListado).setOnClickListener {
            startActivity(Intent(this, ListaLibrosActivity::class.java))
        }
    }

    private fun clearFields() {
        listOf(txtTitulo, txtDescripcion, txtFchPublicacion, txtPrecio, txtStock, txtAutor, txtPortada)
            .forEach { it.text.clear() }
        fechaSeleccionada = 0L
        uriPortada = null
        portadaNombreSeleccionada = ""
    }
}