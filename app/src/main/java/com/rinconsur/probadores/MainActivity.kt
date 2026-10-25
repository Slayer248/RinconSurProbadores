package com.rinconsur.probadores

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.databinding.ActivityMainBinding
import com.rinconsur.probadores.utils.DataInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.AdminFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Ocultar el menú de la Toolbar si estamos en el Login o en Vista Cliente
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.FirstFragment -> {
                    binding.toolbar.menu.clear() // Oculta el menú de la barra
                }
                else -> {
                    invalidateOptionsMenu() // Redibuja el menú en Asistente y Admin
                }
            }
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            DataInitializer.inicializarDatosSiEstaVacio(
                usuarioDao = db.usuarioDao(),
                probadorDao = db.probadorDao()
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Solo infla el menú si la pantalla actual es Admin o Asistente
        val actualId = navController.currentDestination?.id
        if (actualId == R.id.SecondFragment || actualId == R.id.AdminFragment) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Limpia el historial y redirige al Login
                navController.popBackStack(R.id.loginFragment, false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}