package com.libreria.androidproject.ui.activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.libreria.androidproject.R
import com.libreria.androidproject.ui.activity.fragment.ListaLibrosFragment
import com.libreria.androidproject.ui.activity.fragment.ProfileFragment
import com.libreria.androidproject.ui.activity.fragment.RegistrarLibrosFragment

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tabs: TabLayout

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContentView(R.layout.activity_admin_dashboard)

        tabs = findViewById(R.id.tabsAdmin)
        tabs.addTab(tabs.newTab().setText("Registrar"))
        tabs.addTab(tabs.newTab().setText("Listado"))
        tabs.addTab(tabs.newTab().setText("Perfil"))

        showPage(0)

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabReselected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
        })
    }

    fun showPage(pos: Int) {
        val frag = when(pos) {
            0 -> RegistrarLibrosFragment()
            1 -> ListaLibrosFragment()
            else -> ProfileFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerAdmin, frag)
            .commit()
    }

    fun selectTab(index: Int) {
        tabs.getTabAt(index)?.select()
    }
}