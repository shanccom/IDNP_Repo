package com.example.navbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.navbar.ui.theme.NavbarTheme

data class Curso(
    val id: Int,
    val titulo: String,
    val docente: String,
    val horario: String
)

object CursoRepository {
    val listaCursos = listOf(
        Curso(1, "Desarrollo de Aplicaciones Móviles", "Ing. Carlos Ramos", "Lun - Mie 07:00 a 09:00"),
        Curso(2, "Base de Datos Avanzadas", "Dra. Ana Flores", "Mar - Jue 09:00 a 11:00"),
        Curso(3, "Ingeniería de Software", "Mg. Juan Quispe", "Vie 08:00 a 12:00")
    )

    fun obtenerPorId(id: Int): Curso? {
        return listaCursos.find { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavbarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inicio",
        modifier = modifier
    ) {
        // Pantalla 1: Inicio
        composable("inicio") {
            PantallaInicio(
                onIrACursos = { navController.navigate("curso") }
            )
        }

        // Pantalla 2: Lista de Cursos
        composable("curso") {
            PantallaCurso(
                // Al pulsar un curso enviamos SOLO SU ID
                onSeleccionarCurso = { cursoId ->
                    navController.navigate("detalle/$cursoId")
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        // Pantalla 3: Detalle del Curso (Recibe el parámetro {cursoId})
        composable(
            route = "detalle/{cursoId}",
            arguments = listOf(
                navArgument("cursoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // Recuperamos el ID pasado por la ruta
            val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: 1
            // Obtenemos los datos completos (Título, Docente) usando el ID
            val curso = CursoRepository.obtenerPorId(cursoId)

            PantallaDetalle(
                curso = curso,
                onRegresar = { navController.popBackStack() }
            )
        }
    }
}

// ============================================================================
// 4. PANTALLA INICIO
// ============================================================================
@Composable
fun PantallaInicio(onIrACursos: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Inicio", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Práctica: Navegación pasando el ID del Curso",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))

        Button(onClick = onIrACursos) {
            Text(text = "Ver Catálogo de Cursos")
        }
    }
}

@Composable
fun PantallaCurso(
    onSeleccionarCurso: (Int) -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cursos Disponibles", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Selecciona un curso para ver su información (se pasa el ID):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CursoRepository.listaCursos) { curso ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeleccionarCurso(curso.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ID: ${curso.id} • ${curso.titulo}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Docente: ${curso.docente}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "➔", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Regresar a Inicio")
        }
    }
}

// ============================================================================
// 6. PANTALLA DETALLE (Muestra Título y Docente recuperados mediante el ID)
// ============================================================================
@Composable
fun PantallaDetalle(
    curso: Curso?,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Detalle del Curso", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        if (curso != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ID recibido: #${curso.id}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Título del Curso:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = curso.titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Docente a cargo:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = curso.docente,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Horario:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = curso.horario,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Text(text = "Curso no encontrado")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Regresar a Cursos")
        }
    }
}
