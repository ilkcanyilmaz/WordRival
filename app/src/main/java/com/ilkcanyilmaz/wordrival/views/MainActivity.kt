package com.ilkcanyilmaz.wordrival.views

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.interfaces.IOnBackPressed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController
    private var a: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.mainFragment)
        supportActionBar?.hide()
        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> showBottomNav()
                R.id.profileFragment -> showBottomNav()
                R.id.wordFragment -> {
                    showBottomNav()
                    a = 1
                }
                else -> hideBottomNav()
            }
        }
    }
    private var listener: IOnBackPressed? = null

    fun setListener(listener: IOnBackPressed?) {
        this.listener = listener
    }
    private fun showBottomNav() {
        bottomBar.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        bottomBar.visibility = View.GONE

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bottombar, menu)
        bottomBar.setupWithNavController(menu!!, navController)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return true
    }
}