package com.libreria.androidproject.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.libreria.androidproject.R

class UserDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val tabs = findViewById<TabLayout>(R.id.tabsUser)
        val container = findViewById<android.widget.FrameLayout>(R.id.fragmentContainer)

        tabs.addTab(tabs.newTab().setText("Inicio"))
        tabs.addTab(tabs.newTab().setText("Perfil"))

        fun showPage(pos: Int) {
            val layout = when(pos) {
                0 -> R.layout.activity_user_home
                else -> R.layout.fragment_profile
            }
            container.removeAllViews()
            layoutInflater.inflate(layout, container, true)
        }

        showPage(0)
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabReselected(tab: TabLayout.Tab) = showPage(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
        })
    }
}