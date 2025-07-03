package com.libreria.androidproject.ui.activity


import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Libro
import com.libreria.androidproject.repository.FirestoreLibroRepository
import com.libreria.androidproject.util.validateRequired
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RegistrarLibrosFragment : Fragment() {

    private val repo = FirestoreLibroRepository()
    private lateinit var txtTitulo: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtFch: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var txtStock: EditText
    private lateinit var txtAutor: EditText
    private lateinit var txtPortada: EditText

    private var fechaSeleccionada = 0L
    private var uriPortada: Uri? = null
    private var nombrePortada = ""

    private val pickDocLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            uriPortada = it
            var n = ""
            requireContext().contentResolver.query(it, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) n = c.getString(idx)
            }
            txtPortada.setText(n)
            nombrePortada = n
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrar_libros, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txtTitulo = view.findViewById(R.id.txtTitulo)
        txtDescripcion = view.findViewById(R.id.txtDescripcion)
        txtFch = view.findViewById(R.id.txtFchPublicacion)
        txtPrecio = view.findViewById(R.id.txtPrecio)
        txtStock = view.findViewById(R.id.txtStock)
        txtAutor = view.findViewById(R.id.txtAutor)
        txtPortada = view.findViewById(R.id.txtPortada)

        txtFch.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    cal.set(y, m, d)
                    fechaSeleccionada = cal.timeInMillis
                    txtFch.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        txtPortada.setOnClickListener {
            pickDocLauncher.launch(arrayOf("image/*"))
        }

        // Registrar
        view.findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val campos = listOf(txtTitulo, txtDescripcion, txtFch, txtPrecio, txtStock, txtAutor)
            if (!campos.map { it.validateRequired() }.all { it }) return@setOnClickListener

            val libro = Libro(
                titulo = txtTitulo.text.toString(),
                descripcion = txtDescripcion.text.toString(),
                fchpub = Timestamp(Date(fechaSeleccionada)),
                precio = txtPrecio.text.toString().toDouble(),
                stock = txtStock.text.toString().toInt(),
                autor = txtAutor.text.toString(),
                portadaUri = uriPortada?.toString() ?: "",
                portadaNombre = nombrePortada
            )
            lifecycleScope.launch {
                try {
                    val id = repo.addLibro(libro)
                    Toast.makeText(requireContext(), "ID=$id", Toast.LENGTH_SHORT).show()
                    clearFields()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun clearFields() {
        listOf(txtTitulo, txtDescripcion, txtFch, txtPrecio, txtStock, txtAutor, txtPortada)
            .forEach { it.text.clear() }
        fechaSeleccionada = 0L
        uriPortada = null
        nombrePortada = ""
    }
}