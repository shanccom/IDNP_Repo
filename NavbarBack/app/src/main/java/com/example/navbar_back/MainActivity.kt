package com.example.navbar_back

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navbar_back.ui.theme.NavbarBackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavbarBackTheme {
                AppBackStackLab()
            }
        }
    }
}

/**
 * Contenedor principal con el "Visualizador en Vivo de la Pila" (Back Stack Monitor)
 * Muestra en tiempo real cómo crecen y se desapilan las pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBackStackLab() {
    val navController = rememberNavController()

    // Observa el BackStack completo en tiempo real
    val backStackEntries by navController.currentBackStack.collectAsState()
    val activeRoutes = backStackEntries.mapNotNull { it.destination.route }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🥞", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BackStack Lab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            // Visualizador inferior en vivo del historial LIFO
            LiveBackStackViewer(routes = activeRoutes)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 1. Pantalla Inicio
            composable("inicio") {
                PantallaInicio(navController = navController)
            }

            // 2. Pantalla Curso
            composable("curso") {
                PantallaCurso(navController = navController)
            }

            // 3. Pantalla Detalle
            composable("detalle") {
                PantallaDetalle(navController = navController)
            }

            // 4. Pantalla Mensaje
            composable("mensaje") {
                PantallaMensaje(navController = navController)
            }
        }
    }
}

/**
 * Visor visual curioso que muestra cómo está estructurada la pila LIFO
 */
@Composable
fun LiveBackStackViewer(routes: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 PILA ACTUAL (Back Stack en memoria):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${routes.size} pantalla(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (routes.isEmpty()) {
                    Text(
                        text = "(Pila vacía)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                routes.forEachIndexed { index, route ->
                    val isTop = index == routes.lastIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTop) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${index + 1}. $route",
                                fontWeight = if (isTop) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isTop) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isTop) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "★ ACTUAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (index < routes.lastIndex) {
                        Text(
                            text = "➔",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 6.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/* ============================================================================
   1. PANTALLA INICIO (Raíz del Grafo)
   ============================================================================ */
@Composable
fun PantallaInicio(navController: NavController) {
    ScreenLayout(
        title = "Pantalla 1: Inicio",
        badge = "RAÍZ (startDestination)",
        badgeColor = MaterialTheme.colorScheme.primaryContainer,
        iconEmoji = "🏠"
    ) {
        ExplanationCard(
            title = "¿Qué hay en la pila?",
            description = "Solo existe 'inicio'. Al ser la primera pantalla, si llamaras a popBackStack() saldrías de la aplicación porque no hay a dónde retroceder.",
            code = "// Navega y apila 'curso'\nnavController.navigate(\"curso\")"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("curso") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a Curso ➔ (Apilar 'curso')")
        }
    }
}

/* ============================================================================
   2. PANTALLA CURSO (Nivel Intermedio)
   ============================================================================ */
@Composable
fun PantallaCurso(navController: NavController) {
    ScreenLayout(
        title = "Pantalla 2: Curso",
        badge = "NIVEL 2 DE LA PILA",
        badgeColor = MaterialTheme.colorScheme.secondaryContainer,
        iconEmoji = "📚"
    ) {
        ExplanationCard(
            title = "Pila: [inicio ➔ curso]",
            description = "Curso está encima de Inicio. Aquí puedes seguir avanzando a 'Detalle' o desapilar 'Curso' con popBackStack() para regresar a 'Inicio'.",
            code = "// Desapila 'curso' y muestra 'inicio'\nnavController.popBackStack()"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("detalle") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a Detalle ➔ (Apilar 'detalle')")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⬅ Regresar (popBackStack normal)")
        }
    }
}

/* ============================================================================
   3. PANTALLA DETALLE (El punto clave del truco con popUpTo)
   ============================================================================ */
@Composable
fun PantallaDetalle(navController: NavController) {
    ScreenLayout(
        title = "Pantalla 3: Detalle",
        badge = "LABORATORIO DE DECISIÓN",
        badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
        iconEmoji = "🔍"
    ) {
        ExplanationCard(
            title = "¿Qué hace popUpTo con inclusive = true?",
            description = "Normalmente al navegar a 'mensaje' la pila sería [inicio, curso, detalle, mensaje]. Pero con popUpTo(\"detalle\") { inclusive = true }, eliminamos 'detalle' en el camino.",
            code = "navController.navigate(\"mensaje\") {\n" +
                   "    popUpTo(\"detalle\") {\n" +
                   "        inclusive = true // Elimina Detalle de la pila\n" +
                   "    }\n" +
                   "}"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opción 1: El truco curioso de popUpTo inclusive
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🎯 Opción A: Ir a Mensaje BORRANDO Detalle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Usa popUpTo(\"detalle\") { inclusive = true }. 'Detalle' desaparece para siempre de la pila. Al dar 'Atrás' en Mensaje, ¡volverás directo a 'Curso'!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        navController.navigate("mensaje") {
                            popUpTo("detalle") {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir a Mensaje (con popUpTo inclusive)")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Opción 2: Salto directo a la raíz
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚡ Opción B: Salto directo a Inicio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "popBackStack(\"inicio\", inclusive = false) desapila todo hasta llegar a 'inicio', sin borrar 'inicio'.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = {
                        navController.popBackStack("inicio", inclusive = false)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver directo a Inicio (popBackStack)")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⬅ Regresar normal a Curso (popBackStack)")
        }
    }
}

/* ============================================================================
   4. PANTALLA MENSAJE (Comprobación del resultado)
   ============================================================================ */
@Composable
fun PantallaMensaje(navController: NavController) {
    ScreenLayout(
        title = "Pantalla 4: Mensaje",
        badge = "COMPROBACIÓN DEL EFECTO",
        badgeColor = MaterialTheme.colorScheme.errorContainer,
        iconEmoji = "✉️"
    ) {
        ExplanationCard(
            title = "¿Observas la pila abajo?",
            description = "Fíjate en el visor inferior: 'detalle' NO figura. La pila es [inicio, curso, mensaje]. Por ende, al pulsar 'Regresar', regresarás a 'Curso' y NO a 'Detalle'. ¡Se saltó por completo!",
            code = "// Desapila 'mensaje' y aterriza en 'curso'\nnavController.popBackStack()"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⬅ Regresar (Verás que caes en 'Curso')")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                // Volver a la raíz limpiando toda la pila intermedia
                navController.popBackStack("inicio", inclusive = false)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🏠 Resetear todo a Inicio (popBackStack(\"inicio\"))")
        }
    }
}

/* ============================================================================
   COMPONENTES UI AUXILIARES
   ============================================================================ */
@Composable
fun ScreenLayout(
    title: String,
    badge: String,
    badgeColor: Color,
    iconEmoji: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Emoji circular representativo
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconEmoji, fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        SuggestionChip(
            onClick = {},
            label = { Text(badge, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
fun ExplanationCard(
    title: String,
    description: String,
    code: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
