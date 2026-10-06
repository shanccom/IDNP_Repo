package com.example.tarjetascampus_compose

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tarjetascampus_compose.ui.theme.TarjetasCampus_ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TarjetasCampus_ComposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusApp()
                }
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String
) {
    object Home : Screen("home", "Home")
    object Edificios : Screen("edificios", "Edificios")
    object EstiloCuatro : Screen("estilo4", "Estilo 4")

    object Detalle : Screen(
        route = "detalle/{nombre}/{descripcion}/{imagenRes}",
        label = "Detalle"
    ) {
        fun crearRuta(nombre: String, descripcion: String, imagenRes: Int): String {
            return "detalle/${Uri.encode(nombre)}/${Uri.encode(descripcion)}/$imagenRes"
        }
    }
}

@Composable
fun CampusApp() {

    val navController = rememberNavController()

    val items = listOf(Screen.Home, Screen.Edificios, Screen.EstiloCuatro)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val esPantallaDetalle = currentRoute == Screen.Detalle.route

    Scaffold(
        bottomBar = {
            if (!esPantallaDetalle) {
                NavigationBar {
                    items.forEach { screen ->

                        val icon = when (screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Edificios -> Icons.AutoMirrored.Filled.List
                            Screen.EstiloCuatro -> Icons.Default.AutoAwesome
                            Screen.Detalle -> Icons.Default.Home
                        }

                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
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
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(Screen.Home.route) {
                PantallaTarjetas(
                    onAbrirDetalle = { nombre, descripcion, imagenRes ->
                        navController.navigate(
                            Screen.Detalle.crearRuta(nombre, descripcion, imagenRes)
                        )
                    }
                )
            }

            composable(Screen.Edificios.route) {
                EdificiosScreen(
                    onAbrirDetalle = { nombre, descripcion, imagenRes ->
                        navController.navigate(
                            Screen.Detalle.crearRuta(nombre, descripcion, imagenRes)
                        )
                    }
                )
            }

            composable(Screen.EstiloCuatro.route) {
                PropuestaEstilo4Screen(
                    onAbrirDetalle = { nombre, descripcion, imagenRes ->
                        navController.navigate(
                            Screen.Detalle.crearRuta(nombre, descripcion, imagenRes)
                        )
                    }
                )
            }

            composable(
                route = Screen.Detalle.route,
                arguments = listOf(
                    navArgument("nombre") { type = NavType.StringType },
                    navArgument("descripcion") { type = NavType.StringType },
                    navArgument("imagenRes") { type = NavType.IntType }
                )
            ) { backStackEntry ->

                val nombre = backStackEntry.arguments?.getString("nombre").orEmpty()
                val descripcion = backStackEntry.arguments?.getString("descripcion").orEmpty()
                val imagenRes = backStackEntry.arguments?.getInt("imagenRes")
                    ?: R.drawable.biblioteca

                DetalleLugarScreen(
                    nombre = nombre,
                    descripcion = descripcion,
                    imagen = painterResource(id = imagenRes),
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PantallaTarjetas(
    onAbrirDetalle: (String, String, Int) -> Unit = { _, _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Campus",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Estilo 2",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Envoltura con Modifier.clickable (propuesta alternativa al clickable interno)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onAbrirDetalle(
                        "Biblioteca Central",
                        "Zona de estudio y préstamo de libros",
                        R.drawable.biblioteca
                    )
                }
        ) {
            TarjetaCompacta(
                nombre = "Biblioteca Central",
                descripcion = "Zona de estudio y préstamo de libros",
                imagenRes = R.drawable.biblioteca
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onAbrirDetalle(
                        "Comedor Universitario",
                        "Servicio de alimentación para estudiantes",
                        R.drawable.comedor
                    )
                }
        ) {
            TarjetaCompacta(
                nombre = "Comedor Universitario",
                descripcion = "Servicio de alimentación para estudiantes",
                imagenRes = R.drawable.comedor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Estilo 3 · minimalista",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TarjetaEstilo3(
            nombre = "Biblioteca Central",
            descripcion = "Zona de estudio y préstamo",
            etiqueta = "0.4 km",
            imagenRes = R.drawable.biblioteca,
            colorIcono = Color(0xFF1565C0),
            onClick = {
                onAbrirDetalle(
                    "Biblioteca Central",
                    "Zona de estudio y préstamo de libros",
                    R.drawable.biblioteca
                )
            }
        )

        TarjetaEstilo3(
            nombre = "Comedor Universitario",
            descripcion = "Alimentación para estudiantes",
            etiqueta = "0.5 km",
            imagenRes = R.drawable.comedor,
            colorIcono = Color(0xFFEF6C00),
            onClick = {
                onAbrirDetalle(
                    "Comedor Universitario",
                    "Servicio de alimentación para estudiantes",
                    R.drawable.comedor
                )
            }
        )

        TarjetaEstilo3(
            nombre = "Pabellón A",
            descripcion = "Aulas de pregrado",
            etiqueta = "0.7 km",
            imagenRes = R.drawable.pabellon_a,
            colorIcono = Color(0xFF2E7D32),
            onClick = {
                onAbrirDetalle(
                    "Pabellón A",
                    "Aulas de clases para estudiantes de pregrado",
                    R.drawable.pabellon_a
                )
            }
        )

        TarjetaEstilo3(
            nombre = "Auditorio",
            descripcion = "Eventos y conferencias",
            etiqueta = "1.6 km",
            imagenRes = R.drawable.auditorio,
            colorIcono = Color(0xFFF9A825),
            onClick = {
                onAbrirDetalle(
                    "Auditorio",
                    "Eventos, ceremonias y conferencias del campus",
                    R.drawable.auditorio
                )
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Estilo 4 · Alto impacto visual",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TarjetaEstilo4(
            nombre = "Biblioteca Central",
            descripcion = "Espacios modernos de estudio, cubículos grupales y amplio catálogo bibliográfico.",
            categoria = "Estudio",
            distancia = "0.4 km",
            horario = "07:30 - 20:30",
            imagenRes = R.drawable.biblioteca,
            colorTag = Color(0xFF29B6F6),
            onClick = {
                onAbrirDetalle(
                    "Biblioteca Central",
                    "Zona de estudio y préstamo de libros",
                    R.drawable.biblioteca
                )
            }
        )

        TarjetaEstilo4(
            nombre = "Comedor Universitario",
            descripcion = "Alimentación balanceada y menús estudiantiles preparados diariamente en el campus.",
            categoria = "Gastronomía",
            distancia = "0.5 km",
            horario = "11:30 - 15:00",
            imagenRes = R.drawable.comedor,
            colorTag = Color(0xFFFFA726),
            onClick = {
                onAbrirDetalle(
                    "Comedor Universitario",
                    "Servicio de alimentación para estudiantes",
                    R.drawable.comedor
                )
            }
        )
    }
}

@Composable
fun EdificiosScreen(
    onAbrirDetalle: (String, String, Int) -> Unit = { _, _, _ -> }
) {

    val edificios = listOf(
        LugarCampus(
            nombre = "Biblioteca Central",
            descripcion = "Zona de estudio y préstamo de libros",
            imagenRes = R.drawable.biblioteca,
            colorFondo = Color(0xFFE3F2FD),
            distancia = "0.4 km"
        ),
        LugarCampus(
            nombre = "Comedor Universitario",
            descripcion = "Servicio de alimentación para estudiantes",
            imagenRes = R.drawable.comedor,
            colorFondo = Color(0xFFFFF3E0),
            distancia = "0.5 km"
        ),
        LugarCampus(
            nombre = "Pabellón A",
            descripcion = "Aulas de clases para estudiantes de pregrado",
            imagenRes = R.drawable.pabellon_a,
            colorFondo = Color(0xFFE8F5E9),
            distancia = "0.7 km"
        ),
        LugarCampus(
            nombre = "Pabellón B",
            descripcion = "Laboratorios de sistemas y computación",
            imagenRes = R.drawable.pabellon_b,
            colorFondo = Color(0xFFEDE7F6),
            distancia = "0.9 km"
        ),
        LugarCampus(
            nombre = "Auditorio",
            descripcion = "Eventos, ceremonias y conferencias del campus",
            imagenRes = R.drawable.auditorio,
            colorFondo = Color(0xFFFFF8E1),
            distancia = "1.6 km"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Edificios",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "${edificios.size} ubicaciones del campus",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        edificios.forEach { lugar ->

            // Envoltura con Modifier.clickable (propuesta alternativa al clickable interno)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        onAbrirDetalle(
                            lugar.nombre,
                            lugar.descripcion,
                            lugar.imagenRes
                        )
                    }
            ) {
                TarjetaLugar(
                    nombre = lugar.nombre,
                    descripcion = lugar.descripcion,
                    imagen = painterResource(id = lugar.imagenRes),
                    colorFondo = lugar.colorFondo,
                    distancia = lugar.distancia
                )
            }
        }
    }
}

private data class LugarCampus(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int,
    val colorFondo: Color,
    val distancia: String
)

@Composable
fun TarjetaLugar(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    colorFondo: Color,
    distancia: String = ""
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorFondo,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {

            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1
                )

                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                if (distancia.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = distancia,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TarjetaCompacta(
    nombre: String,
    descripcion: String = "",
    imagenRes: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {

        Image(
            painter = painterResource(id = imagenRes),
            contentDescription = nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.35f)
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (descripcion.isNotEmpty()) {
                Text(
                    text = descripcion,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TarjetaEstilo3(
    nombre: String,
    descripcion: String,
    etiqueta: String,
    imagenRes: Int,
    colorFondo: Color = MaterialTheme.colorScheme.surface,
    colorIcono: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorFondo,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, colorIcono.copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = imagenRes),
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = descripcion,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorIcono.copy(alpha = 0.10f)
            ) {
                Text(
                    text = etiqueta,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = colorIcono,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private data class LugarEstilo4(
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val horario: String,
    val distancia: String,
    val imagenRes: Int,
    val colorTag: Color
)

@Composable
fun PropuestaEstilo4Screen(
    onAbrirDetalle: (String, String, Int) -> Unit = { _, _, _ -> }
) {
    val lugaresEstilo4 = listOf(
        LugarEstilo4(
            nombre = "Biblioteca Central",
            descripcion = "Espacios modernos de estudio, salas grupales con Wi-Fi de alta velocidad y amplio catálogo.",
            categoria = "Estudio",
            horario = "07:30 - 20:30",
            distancia = "0.4 km",
            imagenRes = R.drawable.biblioteca,
            colorTag = Color(0xFF29B6F6)
        ),
        LugarEstilo4(
            nombre = "Comedor Universitario",
            descripcion = "Alimentación balanceada y menús estudiantiles preparados diariamente en el campus.",
            categoria = "Gastronomía",
            horario = "11:30 - 15:00",
            distancia = "0.5 km",
            imagenRes = R.drawable.comedor,
            colorTag = Color(0xFFFFA726)
        ),
        LugarEstilo4(
            nombre = "Pabellón A",
            descripcion = "Aulas multimedia climatizadas y áreas dedicadas a tutorías de pregrado.",
            categoria = "Aulas",
            horario = "07:00 - 21:00",
            distancia = "0.7 km",
            imagenRes = R.drawable.pabellon_a,
            colorTag = Color(0xFF66BB6A)
        ),
        LugarEstilo4(
            nombre = "Pabellón B",
            descripcion = "Laboratorios avanzados de cómputo, sistemas embebidos e inteligencia artificial.",
            categoria = "Laboratorios",
            horario = "08:00 - 20:00",
            distancia = "0.9 km",
            imagenRes = R.drawable.pabellon_b,
            colorTag = Color(0xFFAB47BC)
        ),
        LugarEstilo4(
            nombre = "Auditorio Central",
            descripcion = "Auditorio principal equipado con tecnología audiovisual para congresos y ceremonias.",
            categoria = "Eventos",
            horario = "Según eventos",
            distancia = "1.6 km",
            imagenRes = R.drawable.auditorio,
            colorTag = Color(0xFFFF7043)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Propuesta · Estilo 4",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tarjetas de alto impacto visual con imágenes protagonistas, gradientes profundos y datos contextuales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        lugaresEstilo4.forEach { lugar ->
            TarjetaEstilo4(
                nombre = lugar.nombre,
                descripcion = lugar.descripcion,
                categoria = lugar.categoria,
                distancia = lugar.distancia,
                horario = lugar.horario,
                imagenRes = lugar.imagenRes,
                colorTag = lugar.colorTag,
                onClick = {
                    onAbrirDetalle(
                        lugar.nombre,
                        lugar.descripcion,
                        lugar.imagenRes
                    )
                }
            )
        }
    }
}

@Composable
fun TarjetaEstilo4(
    nombre: String,
    descripcion: String,
    categoria: String,
    distancia: String,
    horario: String,
    imagenRes: Int,
    colorTag: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo completa
            Image(
                painter = painterResource(id = imagenRes),
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente superior para realzar badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Gradiente inferior profundo para legibilidad total del contenido
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.95f)
                            ),
                            startY = 120f
                        )
                    )
            )

            // Contenido en capas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Badge categoría + Distancia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de Categoría
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(colorTag)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = categoria.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    // Badge de Distancia
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = distancia,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Bloque Inferior: Nombre, Descripción, Horario y CTA
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 26.sp
                    )

                    Text(
                        text = descripcion,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = horario,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Explorar",
                                    color = Color(0xFF1E1E1E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1E1E),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetalleLugarScreen(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    onVolver: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Detalle del lugar",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                Image(
                    painter = imagen,
                    contentDescription = nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = descripcion,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaTarjetasPreview() {
    TarjetasCampus_ComposeTheme {
        CampusApp()
    }
}
