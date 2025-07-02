package com.libreria.androidproject.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Libro
import com.libreria.androidproject.repository.FirestoreLibroRepository
import com.libreria.androidproject.ui.activity.adapter.LibroAdapter
import com.libreria.androidproject.util.validateRequired
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class ListaLibrosActivity : AppCompatActivity() {

    private val repo = FirestoreLibroRepository()
    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var btnNuevo: Button
    private var libros = mutableListOf<Libro>()
    private lateinit var adapter: LibroAdapter

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
            val nombre = contentResolver.query(it, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                c.getString(idx)
            } ?: ""
            currentPortadaEditText?.setText(nombre)
            portadaNombreTmp = nombre
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_libros)

        listView = findViewById(R.id.lisLibros)
        progress = findViewById(R.id.progress)
        btnNuevo = findViewById(R.id.btnNuevoLibro)

        adapter = LibroAdapter(this, libros)
        listView.adapter = adapter

        btnNuevo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        listView.setOnItemClickListener { _, _, pos, _ ->
            mostrarDialogoEditar(libros[pos])
        }


        // se obtienen  libros
        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            val inicial = withContext(Dispatchers.IO) {
                repo.getAllLibrosFromCache()
            }
            libros.clear()
            libros.addAll(inicial)
            adapter.notifyDataSetChanged()
            progress.visibility = View.GONE
        }

        // se suscriben cambios al firestore
        repo.listenAllLibros { updated ->
            libros.clear()
            libros.addAll(updated)
            runOnUiThread { adapter.notifyDataSetChanged() }
        }
    }

    private fun mostrarDialogoEditar(libro: Libro) {
        fechaTmp = libro.fchpub?.toDate()?.time ?: 0L
        uriTmp = if (libro.portadaUri.isNotEmpty()) Uri.parse(libro.portadaUri) else null
        portadaNombreTmp = libro.portadaNombre
        currentPortadaEditText = null

        val dlgView = layoutInflater.inflate(R.layout.dialog_update_libro, null)

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
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(libro.fchpub?.toDate() ?: Date())
        )
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

        AlertDialog.Builder(this)
            .setTitle("Editar Libro")
            .setView(dlgView)
            .setNegativeButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    repo.deleteLibro(libro.cod)
                    recreate()
                }
            }
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val campos = listOf(etT, etD, etF, etP, etS, etA)
                if (!campos.map { it.validateRequired() }.all { it }) return@setPositiveButton

                libro.apply {
                    titulo        = etT.text.toString()
                    descripcion   = etD.text.toString()
                    fchpub      = Timestamp(Date(fechaTmp))
                    precio        = etP.text.toString().toDouble()
                    stock         = etS.text.toString().toInt()
                    autor         = etA.text.toString()
                    portadaUri    = uriTmp?.toString() ?: portadaUri
                    portadaNombre = portadaNombreTmp
                }
                lifecycleScope.launch {
                    repo.updateLibro(libro)
                    recreate()
                }
            }
            .show()
    }
}