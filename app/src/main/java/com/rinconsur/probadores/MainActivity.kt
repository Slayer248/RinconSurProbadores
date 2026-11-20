package com.rinconsur.probadores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rinconsur.probadores.ui.admin.AdminScreen
import com.rinconsur.probadores.ui.asistente.AsistenteScreen
import com.rinconsur.probadores.ui.auth.AvisoCambioContrasena
import com.rinconsur.probadores.ui.auth.LoginScreen
import com.rinconsur.probadores.ui.cliente.ClienteScreen
import com.rinconsur.probadores.ui.theme.RetailDarkNavy
import com.rinconsur.probadores.ui.theme.RinconSurTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RinconSurTheme {
                val navController = rememberNavController()
                // true solo al abrir la app: permite reingresar solo al turno activo recordado
                var reingresoPendiente by rememberSaveable { mutableStateOf(true) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showLogoutButton = (currentRoute?.startsWith("asistente") == true) || (currentRoute?.startsWith("admin") == true)

                Scaffold(
                    topBar = {
                        // El login tiene su propia cabecera centrada: se oculta la barra estándar
                        if (currentRoute != null && currentRoute != "login") TopAppBar(
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
                                onNavigateToCliente = { username ->
                                    reingresoPendiente = false
                                    navController.navigate("cliente/$username")
                                },
                                onNavigateToAsistente = { username ->
                                    reingresoPendiente = false
                                    navController.navigate("asistente/$username")
                                },
                                onNavigateToAdmin = { username ->
                                    reingresoPendiente = false
                                    navController.navigate("admin/$username")
                                },
                                autoReingresoPendiente = reingresoPendiente,
                                onAutoReingresoConsumido = { reingresoPendiente = false }
                            )
                        }

                        composable(
                            route = "cliente/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: "Cliente"
                            ClienteScreen(
                                username = username,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "asistente/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: "Asistente"
                            AsistenteScreen(
                                username = username
                            )
                            AvisoCambioContrasena(
                                username = username,
                                onCerrarSesion = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "admin/{username}",
                            arguments = listOf(navArgument("username") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: "Admin"
                            AdminScreen(
                                username = username
                            )
                            AvisoCambioContrasena(
                                username = username,
                                onCerrarSesion = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
