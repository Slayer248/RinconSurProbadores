package com.rinconsur.probadores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.ui.admin.AdminScreen
import com.rinconsur.probadores.ui.asistente.AsistenteScreen
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel
import com.rinconsur.probadores.ui.auth.LoginScreen
import com.rinconsur.probadores.ui.cliente.ClienteScreen
import com.rinconsur.probadores.ui.theme.RetailDarkNavy
import com.rinconsur.probadores.ui.theme.RinconSurTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.rinconsur.probadores.utils.DataInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AsistenteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            DataInitializer.inicializarDatosSiEstaVacio(
                usuarioDao = db.usuarioDao(),
                probadorDao = db.probadorDao(),
            )
        }

        setContent {
            RinconSurTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showLogoutButton = (currentRoute?.startsWith("asistente") == true) || (currentRoute?.startsWith("admin") == true)

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "Logo Rincón Sur Probadores",
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(34.dp)
                                    )
                                    Text(
                                        text = "Rincón Sur Probadores",
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = RetailDarkNavy,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            actions = {
                                if (showLogoutButton) {
                                    IconButton(
                                        onClick = {
                                            navController.navigate("login") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Cerrar Sesión"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToCliente = { username ->
                                    navController.navigate("cliente/$username")
                                },
                                onNavigateToAsistente = { username ->
                                    navController.navigate("asistente/$username")
                                },
                                onNavigateToAdmin = { username ->
                                    navController.navigate("admin/$username")
                                }
                            )
                        }

                        composable(
                            route = "cliente/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: "Cliente"
                            ClienteScreen(
                                username = username,
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "asistente/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: "Asistente"
                            AsistenteScreen(
                                username = username,
                                viewModel = viewModel,
                                onNavigateToCliente = { name ->
                                    navController.navigate("cliente/$name")
                                }
                            )
                        }

                        composable(
                            route = "admin/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) {
                            AdminScreen(
                                viewModel = viewModel,
                                onNavigateToAsistente = { name ->
                                    navController.navigate("asistente/$name")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
