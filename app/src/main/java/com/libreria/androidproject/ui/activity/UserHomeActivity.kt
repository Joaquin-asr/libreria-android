package com.libreria.androidproject.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.libreria.androidproject.R
import com.libreria.androidproject.model.Libro
import com.libreria.androidproject.repository.FirestoreLibroRepository
import com.libreria.androidproject.ui.activity.adapter.BookHorizontalAdapter
import kotlinx.coroutines.launch

class UserHomeActivity : AppCompatActivity() {

    private val repo = FirestoreLibroRepository()
    private var todos = listOf<Libro>()

    private lateinit var rvFeatured: RecyclerView
    private lateinit var rvNewArrivals: RecyclerView
    private lateinit var rvCategories: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        rvFeatured    = findViewById(R.id.rvFeatured)
        rvNewArrivals = findViewById(R.id.rvNewArrivals)
        rvCategories  = findViewById(R.id.rvCategories)

        rvFeatured.layoutManager    = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvNewArrivals.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            todos = repo.getAllLibrosFromCache()

            val adapter = BookHorizontalAdapter(todos) { libro ->
                startActivity(
                    Intent(this@UserHomeActivity, BookDetailActivity::class.java)
                        .putExtra("libroId", libro.cod)
                )
            }

            rvFeatured.adapter    = adapter
            rvNewArrivals.adapter = adapter
            rvCategories.adapter  = adapter
        }
    }
}