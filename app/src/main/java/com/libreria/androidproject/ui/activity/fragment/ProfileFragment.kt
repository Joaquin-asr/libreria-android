package com.libreria.androidproject.ui.activity.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.libreria.androidproject.R
import com.libreria.androidproject.auth.FirebaseAuthManager
import com.libreria.androidproject.auth.LoginActivity
import com.libreria.androidproject.repository.FirestoreOrderRepository
import com.libreria.androidproject.repository.FirestoreUserRepository
import com.libreria.androidproject.ui.activity.adapter.OrderAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private val userRepo  = FirestoreUserRepository()
    private val orderRepo = FirestoreOrderRepository()

    private lateinit var btnLogout     : MaterialButton
    private lateinit var tvEmail       : MaterialTextView
    private lateinit var tvOrdersLabel : View
    private lateinit var rvOrders      : RecyclerView
    private lateinit var orderAdapter  : OrderAdapter
    private lateinit var progressBar: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnLogout     = view.findViewById(R.id.btnLogout)
        tvEmail       = view.findViewById(R.id.tvUserName)
        tvOrdersLabel = view.findViewById(R.id.tvOrders)
        rvOrders      = view.findViewById(R.id.rvOrders)
        progressBar = view.findViewById(R.id.progressBarProfile)

        rvOrders.layoutManager = LinearLayoutManager(requireContext())
        orderAdapter = OrderAdapter()
        rvOrders.adapter = orderAdapter

        btnLogout.setOnClickListener {
            FirebaseAuthManager.signOut()
            startActivity(Intent(requireActivity(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        loadProfileAndOrders()
    }

    override fun onResume() {
        super.onResume()
        loadProfileAndOrders()
    }

    private fun loadProfileAndOrders() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val user = FirebaseAuthManager.currentUser() ?: return@launch
            val profile = userRepo.getUser(user.uid) ?: return@launch

            tvEmail.text = profile.email

            if (profile.role == "Usuario") {
                val orders = orderRepo.getOrdersForUser(user.uid)

                if (orders.isNotEmpty()) {
                    val libroIds = orders.map { it.libroId }.distinct()
                    val libroSnaps = FirebaseFirestore.getInstance()
                        .collection("libros")
                        .whereIn(FieldPath.documentId(), libroIds)
                        .get()
                        .await()

                    val titleMap = libroSnaps.documents.associate { doc ->
                        doc.id to (doc.getString("titulo") ?: "Título desconocido")
                    }

                    orderAdapter.update(orders, titleMap)
                    tvOrdersLabel.visibility = View.VISIBLE
                    rvOrders.visibility = View.VISIBLE
                } else {
                    orderAdapter.update(emptyList(), emptyMap())
                    tvOrdersLabel.visibility = View.VISIBLE
                    rvOrders.visibility = View.GONE
                }
            } else {
                tvOrdersLabel.visibility = View.GONE
                rvOrders.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
        }
    }
}