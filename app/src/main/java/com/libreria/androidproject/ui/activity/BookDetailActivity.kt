package com.libreria.androidproject.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Order
import com.libreria.androidproject.repository.FirestoreOrderRepository
import com.google.firebase.Timestamp
import com.libreria.androidproject.auth.FirebaseAuthManager
import kotlinx.coroutines.launch

class BookDetailActivity : AppCompatActivity() {
    private val orderRepo = FirestoreOrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        val libroId = intent.getStringExtra("libroId") ?: return

        findViewById<Button>(R.id.btnBuy).setOnClickListener {
            lifecycleScope.launch {
                val uid = FirebaseAuthManager.currentUser()?.uid ?: return@launch
                val order = Order(
                    userId    = uid,
                    libroId   = libroId,
                    timestamp = Timestamp.now()
                )
                orderRepo.addOrder(order)
                Toast.makeText(this@BookDetailActivity, "¡Libro comprado!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}