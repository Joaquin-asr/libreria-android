package com.libreria.androidproject.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.libreria.androidproject.R
import com.libreria.androidproject.ui.activity.fragment.BookDetailFragment
import com.libreria.androidproject.ui.activity.fragment.ProfileFragment
import com.libreria.androidproject.ui.activity.fragment.UserHomeFragment

class UserDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val tabs = findViewById<TabLayout>(R.id.tabsUser)
        tabs.addTab(tabs.newTab().setText("Inicio"))
        tabs.addTab(tabs.newTab().setText("Perfil"))

        showPage(0)

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabReselected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showPage(pos: Int) {
        val frag: Fragment = when (pos) {
            0 -> UserHomeFragment()
            else -> ProfileFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, frag)
            .commit()
    }

    fun showBookDetail(libroId: String) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainer, BookDetailFragment.newInstance(libroId))
            .commit()
    }
}