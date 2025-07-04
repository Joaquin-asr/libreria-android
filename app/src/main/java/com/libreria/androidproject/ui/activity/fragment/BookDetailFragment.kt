package com.libreria.androidproject.ui.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.libreria.androidproject.R
import com.libreria.androidproject.auth.FirebaseAuthManager
import com.libreria.androidproject.model.Libro
import com.libreria.androidproject.model.Order
import com.libreria.androidproject.repository.FirestoreOrderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookDetailFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val orderRepo = FirestoreOrderRepository()

    companion object {
        private const val ARG_LIBRO_ID = "libroId"
        fun newInstance(libroId: String) = BookDetailFragment().apply {
            arguments = Bundle().also { it.putString(ARG_LIBRO_ID, libroId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_book_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pb       = view.findViewById<ProgressBar>(R.id.pbDetail)
        val ivCover  = view.findViewById<ImageView>(R.id.imgDetailPortada)
        val tvTitle  = view.findViewById<TextView>(R.id.tvDetailTitulo)
        val tvAuthor = view.findViewById<TextView>(R.id.tvDetailAutor)
        val tvPrice  = view.findViewById<TextView>(R.id.tvDetailPrecio)
        val tvDesc   = view.findViewById<TextView>(R.id.tvDetailDescripcion)
        val btnBuy   = view.findViewById<Button>(R.id.btnBuy)

        val libroId = arguments?.getString(ARG_LIBRO_ID)
        if (libroId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Libro inválido", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        lifecycleScope.launch {
            try {
                pb.visibility = View.VISIBLE

                val snap = firestore
                    .collection("libros")
                    .document(libroId)
                    .get(Source.CACHE)
                    .await()

                val libro = snap.toObject(Libro::class.java)
                    ?: throw Exception("No se encontró el libro")

                tvTitle.text  = libro.titulo
                tvAuthor.text = "Por ${libro.autor}"
                tvPrice.text  = "S/. %.2f".format(libro.precio)
                tvDesc.text   = libro.descripcion

                if (libro.portadaUri.isNotEmpty()) {
                    Glide.with(this@BookDetailFragment)
                        .load(libro.portadaUri)
                        .placeholder(R.drawable.ic_book)
                        .into(ivCover)
                }

                val uid = FirebaseAuthManager.currentUser()?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "No autenticado", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                    return@launch
                }

                val existing = orderRepo
                    .getOrdersForUser(uid)
                    .any { it.libroId == libroId }

                if (existing) {
                    btnBuy.text = "Libro Comprado"
                    btnBuy.isEnabled = false
                } else {
                    btnBuy.text = "Comprar Libro"
                    btnBuy.isEnabled = true

                    btnBuy.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                val order = Order(
                                    userId    = uid,
                                    libroId   = libroId,
                                    timestamp = Timestamp.now()
                                )
                                orderRepo.addOrder(order)
                                Toast.makeText(requireContext(), "¡Libro comprado!", Toast.LENGTH_SHORT).show()

                                btnBuy.text = "Libro Comprado"
                                btnBuy.isEnabled = false
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Error al comprar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar libro", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } finally {
                pb.visibility = View.GONE
            }
        }
    }
}