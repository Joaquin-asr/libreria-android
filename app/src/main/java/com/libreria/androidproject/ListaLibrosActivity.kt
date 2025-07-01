package com.libreria.androidproject

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.*


class ListaLibrosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var listView: ListView
    private lateinit var btnNuevo: Button
    private var libros = mutableListOf<Libro>()

    private var fechaTmp: Long = 0L
    private var uriTmp: Uri? = null
    private var portadaNombreTmp: String = ""
    private var currentPortadaEditText: EditText? = null

    private val pickDocForEditLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uriTmp = it

            var nombreArchivo = ""
            contentResolver.query(it, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) nombreArchivo = c.getString(idx)
            }
            currentPortadaEditText?.setText(nombreArchivo)
            portadaNombreTmp = nombreArchivo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_libros)

        listView = findViewById(R.id.lisLibros)
        btnNuevo = findViewById(R.id.btnNuevoLibro)
        listView.adapter = LibroAdapter(this, libros)

        btnNuevo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        listView.setOnItemClickListener { _, _, pos, _ ->
            mostrarDialogoEditar(libros[pos])
        }

        // aqui cargamos lista
        db.collection("libros")
            .get(Source.CACHE)
            .addOnSuccessListener { snapshot ->
                libros.clear()
                snapshot.documents.forEach { doc ->
                    val l = doc.toObject(Libro::class.java)!!.apply { cod = doc.id }
                    libros.add(l)
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

        //aqui se escucha cambios desde el serv
        db.collection("libros")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                libros.clear()
                snapshot.documents.forEach { doc ->
                    val l = doc.toObject(Libro::class.java)!!.apply { cod = doc.id }
                    libros.add(l)
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
    }

    private fun mostrarDialogoEditar(libro: Libro) {
        fechaTmp = libro.fchpub
        uriTmp = if (libro.portadaUri.isNotEmpty()) Uri.parse(libro.portadaUri) else null
        portadaNombreTmp = libro.portadaNombre
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
        etF.setText(java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(libro.fchpub)))
        etP.setText(libro.precio.toString())
        etS.setText(libro.stock.toString())
        etA.setText(libro.autor)
        if (libro.portadaNombre.isNotEmpty()) {
            etPo.setText(libro.portadaNombre)
        }

        etF.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = fechaTmp }
            DatePickerDialog(this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    fechaTmp = cal.timeInMillis
                    etF.setText(java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(cal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        etPo.setOnClickListener {
            currentPortadaEditText = etPo
            pickDocForEditLauncher.launch(arrayOf("image/*"))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Libro")
            .setView(dlgView)
            .setNegativeButton("Eliminar") { _, _ ->
                db.collection("libros").document(libro.cod)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
            }
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                libro.apply {
                    titulo      = etT.text.toString()
                    descripcion = etD.text.toString()
                    fchpub      = fechaTmp
                    precio      = etP.text.toString().toDouble()
                    stock       = etS.text.toString().toInt()
                    autor       = etA.text.toString()
                    portadaUri  = uriTmp?.toString() ?: ""
                    portadaNombre = portadaNombreTmp
                }
                db.collection("libros").document(libro.cod)
                    .set(libro.toMap())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
            }
            .show()
    }
}