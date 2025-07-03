package com.libreria.androidproject.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
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



class ListaLibrosFragment : Fragment() {

    private val repo = FirestoreLibroRepository()
    private var listenerReg: ListenerRegistration? = null
    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var btnNuevo: Button
    private var libros = mutableListOf<Libro>()
    private lateinit var adapter: LibroAdapter

    private var fechaTmp = 0L
    private var uriTmp: Uri? = null
    private var nombreTmp = ""
    private var currentPortadaEt: EditText? = null

    private val pickLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            uriTmp = it
            var n = ""
            requireContext().contentResolver.query(it, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) n = c.getString(idx)
            }
            currentPortadaEt?.setText(n)
            nombreTmp = n
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_libros, container, false)
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        listView = view.findViewById(R.id.lisLibros)
        progress = view.findViewById(R.id.progress)
        btnNuevo = view.findViewById(R.id.btnNuevoLibro)

        adapter = LibroAdapter(requireContext(), libros)
        listView.adapter = adapter

        btnNuevo.setOnClickListener {
            // mostrar pestaña Registrar:
            (activity as? AdminDashboardActivity)?.selectTab(0)
        }

        listView.setOnItemClickListener { _, _, pos, _ ->
            mostrarDialogo(libros[pos])
        }

        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            val datos = withContext(Dispatchers.IO) { repo.getAllLibrosFromCache() }
            libros.clear()
            libros.addAll(datos)
            adapter.notifyDataSetChanged()
            progress.visibility = View.GONE
        }

        listenerReg = repo.listenAllLibros { updated ->
            libros.clear()
            libros.addAll(updated)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerReg?.remove()
        listenerReg = null
    }

    private fun mostrarDialogo(libro: Libro) {
        fechaTmp = libro.fchpub?.toDate()?.time ?: 0
        uriTmp = libro.portadaUri.takeIf { it.isNotEmpty() }?.let(Uri::parse)
        nombreTmp = libro.portadaNombre
        currentPortadaEt = null

        val dlg = layoutInflater.inflate(R.layout.dialog_update_libro, null)
        val etT = dlg.findViewById<EditText>(R.id.etTitulo)
        val etD = dlg.findViewById<EditText>(R.id.etDescripcion)
        val etF = dlg.findViewById<EditText>(R.id.etFchPub)
        val etP = dlg.findViewById<EditText>(R.id.etPrecio)
        val etS = dlg.findViewById<EditText>(R.id.etStock)
        val etA = dlg.findViewById<EditText>(R.id.etAutor)
        val etPo= dlg.findViewById<EditText>(R.id.etPortada)

        etT.setText(libro.titulo)
        etD.setText(libro.descripcion)
        etF.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(libro.fchpub?.toDate() ?: Date()))
        etP.setText(libro.precio.toString())
        etS.setText(libro.stock.toString())
        etA.setText(libro.autor)
        if (libro.portadaNombre.isNotEmpty()) etPo.setText(libro.portadaNombre)

        etF.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = fechaTmp }
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    cal.set(y, m, d)
                    fechaTmp = cal.timeInMillis
                    etF.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time))
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        etPo.setOnClickListener {
            currentPortadaEt = etPo
            pickLauncher.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Libro")
            .setView(dlg)
            .setNegativeButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    repo.deleteLibro(libro.cod)
                }
            }
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val campos = listOf(etT, etD, etF, etP, etS, etA)
                if (!campos.map { it.validateRequired() }.all { it }) return@setPositiveButton
                libro.apply {
                    titulo      = etT.text.toString()
                    descripcion = etD.text.toString()
                    fchpub      = Timestamp(Date(fechaTmp))
                    precio      = etP.text.toString().toDouble()
                    stock       = etS.text.toString().toInt()
                    autor       = etA.text.toString()
                    portadaUri  = uriTmp?.toString() ?: portadaUri
                    portadaNombre = nombreTmp
                }
                lifecycleScope.launch { repo.updateLibro(libro) }
            }
            .show()
    }
}