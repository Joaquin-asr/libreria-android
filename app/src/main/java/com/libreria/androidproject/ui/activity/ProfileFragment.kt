package com.libreria.androidproject.ui.activity


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.libreria.androidproject.R
import com.libreria.androidproject.auth.FirebaseAuthManager
import com.libreria.androidproject.auth.LoginActivity
import com.libreria.androidproject.repository.FirestoreOrderRepository
import com.libreria.androidproject.repository.FirestoreUserRepository
import com.libreria.androidproject.ui.activity.adapter.OrderAdapter
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    private val userRepo  = FirestoreUserRepository()
    private val orderRepo = FirestoreOrderRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val tvEmail   = view.findViewById<MaterialTextView>(R.id.tvUserName)
        val tvOrdersL = view.findViewById<View>(R.id.tvOrders)
        val rvOrders  = view.findViewById<RecyclerView>(R.id.rvOrders)

        btnLogout.setOnClickListener {
            FirebaseAuthManager.signOut()
            startActivity(Intent(requireActivity(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        lifecycleScope.launch {
            val user = FirebaseAuthManager.currentUser() ?: return@launch
            val profile = userRepo.getUser(user.uid) ?: return@launch
            tvEmail.text = profile.email

            if (profile.role == "Usuario") {
                val orders = orderRepo.getOrdersForUser(profile.uid)
                rvOrders.adapter = OrderAdapter(orders)
            } else {
                tvOrdersL.visibility = View.GONE
                rvOrders.visibility = View.GONE
            }
        }
    }
}