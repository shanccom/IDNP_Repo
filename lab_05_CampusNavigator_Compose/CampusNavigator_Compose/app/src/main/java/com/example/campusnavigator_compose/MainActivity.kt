package com.example.campusnavigator_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campusnavigator_compose.ui.theme.CampusNavigator_ComposeTheme
import com.example.campusnavigator_compose.viewmodel.SeleccionViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CampusNavigator_ComposeTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String
) {    object Home : Screen("home", "Home")
    object Edificios : Screen("edificios", "Edificios")
    object Mapa : Screen("mapa", "Mapa")
    object Perfil : Screen("perfil", "Perfil")
}

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val seleccionViewModel: SeleccionViewModel = viewModel()

    val items = listOf( Screen.Home, Screen.Edificios, Screen.Mapa, Screen.Perfil )

    Scaffold(

        bottomBar = {

            NavigationBar {

                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->

                    val icon = when (screen) {

                        Screen.Home ->
                            Icons.Default.Home

                        Screen.Edificios ->
                            Icons.AutoMirrored.Filled.List

                        Screen.Mapa ->
                            Icons.Default.Place

                        Screen.Perfil ->
                            Icons.Default.Person
                    }

                    NavigationBarItem(

                        selected = currentRoute == screen.route,

                        onClick = {

                            navController.navigate(screen.route) {

                                popUpTo(
                                    navController.graph.startDestinationId
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.label
                            )
                        },

                        label = {
                            Text(screen.label)
                        }
                    )
                }
            }
        }

    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(Screen.Home.route) {

                HomeScreen(
                    viewModel = seleccionViewModel
                )
            }

            composable(Screen.Edificios.route) {

                EdificiosScreen(
                    viewModel = seleccionViewModel,
                    navController = navController
                )
            }

            composable(Screen.Mapa.route) {

                MapaScreen()
            }

            composable(Screen.Perfil.route) {

                PerfilScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: SeleccionViewModel
) {

    val edificioSeleccionado by viewModel.edificioSeleccionadoFlow.collectAsState()

    val sinSeleccion = edificioSeleccionado == SeleccionViewModel.SIN_SELECCION

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {

        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,

            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "Campus Navigator",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Universidad Nacional de San Agustín",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,

            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Icon(
                    imageVector = if (sinSeleccion) Icons.Default.Search else Icons.Default.Place,
                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )

                Column {

                    Text(
                        text = "Último edificio consultado",
                        style = MaterialTheme.typography.labelMedium,

                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = edificioSeleccionado,
                        style = MaterialTheme.typography.titleMedium,

                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ResumenCard(
                valor = "4",
                etiqueta = "Edificios",
                modifier = Modifier.weight(1f)
            )

            ResumenCard(
                valor = "3",
                etiqueta = "Favoritos",
                modifier = Modifier.weight(1f)
            )

            ResumenCard(
                valor = "7",
                etiqueta = "Rutas",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResumenCard(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,

                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EdificiosScreen(
    viewModel: SeleccionViewModel,
    navController: NavHostController
) {

    val edificios = listOf(
        Edificio("Biblioteca Central", "Biblioteca", "0.4 km", Icons.Default.MenuBook),
        Edificio("Pabellón A", "Aulas", "0.7 km", Icons.Default.School),
        Edificio("Pabellón B", "Laboratorios", "1.1 km", Icons.Default.Apartment),
        Edificio("Auditorio", "Eventos", "1.6 km", Icons.Default.Campaign)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Column(
                modifier = Modifier.padding(bottom = 4.dp)
            ) {

                Text(
                    text = "Edificios",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "${edificios.size} ubicaciones registradas en el campus",
                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(edificios) { edificio ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),

                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = edificio.icon,
                            contentDescription = null,

                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = edificio.nombre,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "${edificio.categoria} · ${edificio.distancia}",
                            style = MaterialTheme.typography.bodySmall,

                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {

                            viewModel.seleccionarEdificio(edificio.nombre)

                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {

                        Text("Ver")
                    }
                }
            }
        }
    }
}

@Composable
fun MapaScreen() {

    val colorLinea = MaterialTheme.colorScheme.outlineVariant

    val colorPin = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Mapa",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Ubicación de los edificios del campus",
                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                val paso = size.width / 6f

                for (columna in 1..5) {

                    val x = paso * columna

                    drawLine(
                        color = colorLinea,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height)
                    )
                }

                for (fila in 1..8) {

                    val y = size.height / 9f * fila

                    drawLine(
                        color = colorLinea,
                        start = Offset(0f, y),
                        end = Offset(size.width, y)
                    )
                }

                listOf(
                    Offset(0.22f, 0.28f),
                    Offset(0.72f, 0.20f),
                    Offset(0.38f, 0.58f),
                    Offset(0.80f, 0.68f)
                ).forEach { posicion ->

                    drawCircle(
                        color = colorPin.copy(alpha = 0.35f),
                        radius = 22.dp.toPx(),
                        center = Offset(
                            size.width * posicion.x,
                            size.height * posicion.y
                        )
                    )

                    drawCircle(
                        color = colorPin,
                        radius = 8.dp.toPx(),
                        center = Offset(
                            size.width * posicion.x,
                            size.height * posicion.y
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = "Mapa pendiente de integrar",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aquí se mostrará la ubicación de cada edificio",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,

                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        FilledTonalButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null,

                modifier = Modifier.size(18.dp)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text("Centrar en mi ubicación")
        }
    }
}

@Composable
fun PerfilScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {

        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "UN",
                        style = MaterialTheme.typography.headlineMedium,

                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "Estudiante UNSA",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Ingeniería de Sistemas",
                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                PerfilFila(
                    icon = Icons.Default.School,
                    titulo = "Facultad",
                    valor = "FISICO"
                )

                HorizontalDivider()

                PerfilFila(
                    icon = Icons.Default.Star,
                    titulo = "Edificios favoritos",
                    valor = "3 guardados"
                )

                HorizontalDivider()

                PerfilFila(
                    icon = Icons.Default.Edit,
                    titulo = "Editar perfil",
                    valor = "Próximamente"
                )
            }
        }

        Text(
            text = "Pantalla de marcador de posición",
            style = MaterialTheme.typography.labelMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PerfilFila(
    icon: ImageVector,
    titulo: String,
    valor: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = valor,
                style = MaterialTheme.typography.bodySmall,

                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class Edificio(
    val nombre: String,
    val categoria: String,
    val distancia: String,
    val icon: ImageVector
)
