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
import com.libreria.androidproject.R
import com.libreria.androidproject.repository.FirestoreLibroRepository
import com.libreria.androidproject.ui.activity.adapter.BookHorizontalAdapter
import kotlinx.coroutines.launch

class UserHomeFragment : Fragment() {
    private val repo = FirestoreLibroRepository()
    private lateinit var rvFeatured: RecyclerView
    private lateinit var rvNewArrivals: RecyclerView
    private lateinit var rvCategories: RecyclerView
    private lateinit var progressBar: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_user_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvFeatured    = view.findViewById(R.id.rvFeatured)
        rvNewArrivals = view.findViewById(R.id.rvNewArrivals)
        rvCategories  = view.findViewById(R.id.rvCategories)
        progressBar = view.findViewById(R.id.progressBarHome)



        rvFeatured.layoutManager    = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvNewArrivals.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategories.layoutManager  = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val todos = repo.getAllLibrosFromCache()

            val adapter = BookHorizontalAdapter(todos) { libro ->
                startActivity(
                    Intent(requireContext(), BookDetailFragment::class.java)
                        .putExtra("libroId", libro.cod)
                )
            }

            rvFeatured.adapter    = adapter
            rvNewArrivals.adapter = adapter
            rvCategories.adapter  = adapter
            progressBar.visibility = View.GONE

        }
    }
}