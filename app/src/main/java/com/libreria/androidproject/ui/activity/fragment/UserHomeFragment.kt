package com.libreria.androidproject.ui.activity.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.libreria.androidproject.R
import com.libreria.androidproject.repository.FirestoreLibroRepository
import com.libreria.androidproject.ui.activity.UserDashboardActivity
import com.libreria.androidproject.ui.activity.adapter.BookHorizontalAdapter
import kotlinx.coroutines.launch

class UserHomeFragment : Fragment() {
    private val repo = FirestoreLibroRepository()

    private lateinit var rvBooks: RecyclerView
    private lateinit var progressBar: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_user_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvBooks     = view.findViewById(R.id.rvBooks)
        progressBar = view.findViewById(R.id.progressBarHome)

        rvBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val todos = repo.getAllLibrosFromCache()

            rvBooks.adapter = BookHorizontalAdapter(todos) { libro ->
                (requireActivity() as? UserDashboardActivity)
                    ?.showBookDetail(libro.cod)
            }

            progressBar.visibility = View.GONE
        }
    }
}