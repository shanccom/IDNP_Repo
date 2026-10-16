
package com.example.planointeractivo_compose

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.planointeractivo_compose.ui.theme.PlanoInteractivo_ComposeTheme
import com.example.planointeractivo_compose.viewmodel.PlanoViewModel

// Espacio de diseño fijo: el plano se escala para llenar el ancho de pantalla
private const val ANCHO_DISENO = 360f
private const val ALTO_DISENO = 430f

// Representa cada marcador del plano
data class Marcador(
    val offset: Offset,
    val radio: Float,
    val titulo: String,
    val descripcion: String
)

// Representa un ambiente (salón o patio) del plano
data class Ambiente(
    val topLeft: Offset,
    val size: Size,
    val color: Color,
    val etiqueta: String
)

// Ambientes del Pabellón de Ingeniería - UNSA
private val ambientesPabellon = listOf(
    Ambiente(Offset(20f, 60f), Size(160f, 110f), Color(0xFF80DEEA), "Aula A101"),
    Ambiente(Offset(195f, 60f), Size(155f, 110f), Color(0xFFA5D6A7), "Aula A102"),
    Ambiente(Offset(20f, 190f), Size(160f, 110f), Color(0xFFCE93D8), "Lab. Cómputo"),
    Ambiente(Offset(195f, 190f), Size(155f, 110f), Color(0xFFFFE082), "Patio Central"),
    Ambiente(Offset(20f, 320f), Size(160f, 80f), Color(0xFF90CAF9), "Biblioteca"),
    Ambiente(Offset(195f, 320f), Size(155f, 80f), Color(0xFFFFAB91), "Cafetería")
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlanoInteractivo_ComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlanoApp()
                }
            }
        }
    }
}

// Rutas de navegación de la aplicación
sealed class Pantalla(val route: String) {
    object Mapa : Pantalla("mapa")
    object Plano : Pantalla("plano")
}

@Composable
fun PlanoApp() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Pantalla.Mapa.route
    ) {

        composable(Pantalla.Mapa.route) {
            MapaScreen(
                onAbrirPlano = { navController.navigate(Pantalla.Plano.route) }
            )
        }

        composable(Pantalla.Plano.route) {
            PlanoScreen(
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

// Pantalla de mapa con la tarjeta que abre el plano interactivo
@Composable
fun MapaScreen(onAbrirPlano: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Mapa del Campus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Explora la ubicación de los edificios de la UNSA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onAbrirPlano),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Pabellón de Ingeniería",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Plano interactivo · 6 ambientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(valor = "6", etiqueta = "Ambientes", modifier = Modifier.weight(1f))
                StatCard(valor = "6", etiqueta = "Marcadores", modifier = Modifier.weight(1f))
                StatCard(valor = "1", etiqueta = "Pabellón", modifier = Modifier.weight(1f))
            }

            Text(
                text = "Toca la tarjeta para abrir el plano interactivo.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatCard(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Pantalla que muestra el plano interactivo (con botón para volver)
@Composable
fun PlanoScreen(onVolver: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Column {
                Text(
                    text = "Plano interactivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pabellón de Ingeniería - UNSA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            PlanoAmbiente()
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Ambientes del pabellón",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                ambientesPabellon.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        fila.forEach { ambiente ->
                            LeyendaItem(
                                ambiente = ambiente,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(
    ambiente: Ambiente,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(ambiente.color)
        )

        Text(
            text = ambiente.etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun PlanoAmbiente(viewModel: PlanoViewModel = viewModel()) {

    PlanoContenido(
        marcadores = viewModel.marcadores,
        marcadorSeleccionado = viewModel.marcadorSeleccionado,
        onMarcadorClick = { marcador -> viewModel.seleccionarMarcador(marcador) },
        onCerrar = { viewModel.limpiarSeleccion() }
    )
}

@Composable
fun PlanoContenido(
    marcadores: List<Marcador>,
    marcadorSeleccionado: Marcador?,
    onMarcadorClick: (Marcador?) -> Unit,
    onCerrar: () -> Unit
) {

    // Lienzo interactivo
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .pointerInput(marcadores) {
                detectTapGestures { tap ->

                    // Convertir el toque a coordenadas del espacio de diseño
                    val escala = minOf(
                        size.width / ANCHO_DISENO,
                        size.height / ALTO_DISENO
                    ) * 0.88f
                    val despX = (size.width - ANCHO_DISENO * escala) / 2f
                    val despY = (size.height - ALTO_DISENO * escala) / 2f
                    val punto = Offset((tap.x - despX) / escala, (tap.y - despY) / escala)

                    onMarcadorClick(
                        marcadores.find { marcador ->
                            (punto - marcador.offset).getDistance() <= marcador.radio
                        }
                    )
                }
            }
    ) {

        // Escalar y centrar todo el plano en la pantalla
        val escala = minOf(
            size.width / ANCHO_DISENO,
            size.height / ALTO_DISENO
        ) * 0.88f
        val despX = (size.width - ANCHO_DISENO * escala) / 2f
        val despY = (size.height - ALTO_DISENO * escala) / 2f

        withTransform({
            translate(despX, despY)
            scale(escala, escala, pivot = Offset.Zero)
        }) {

            // Título del plano
            drawIntoCanvas { canvas ->
                val paintTitulo = Paint().apply {
                    color = android.graphics.Color.rgb(38, 50, 56)
                    textSize = 30f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText("Pabellón de Ingeniería - UNSA", 185f, 42f, paintTitulo)
            }

            // Contorno de la edificación
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(10f, 55f),
                size = Size(350f, 355f),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 6f)
            )

            // Dibujar cada ambiente con su etiqueta visible
            ambientesPabellon.forEach { ambiente ->
                drawRoundRect(
                    color = ambiente.color,
                    topLeft = ambiente.topLeft,
                    size = ambiente.size,
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = Color(0xFF546E7A),
                    topLeft = ambiente.topLeft,
                    size = ambiente.size,
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 3f)
                )

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = android.graphics.Color.rgb(38, 50, 56)
                        textSize = 22f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                    val cx = ambiente.topLeft.x + ambiente.size.width / 2f
                    val cy = ambiente.topLeft.y + ambiente.size.height - 14f
                    canvas.nativeCanvas.drawText(ambiente.etiqueta, cx, cy, paint)
                }
            }

            // Pasillos divisores horizontales
            drawLine(Color(0xFF546E7A), Offset(20f, 175f), Offset(350f, 175f), strokeWidth = 4f)
            drawLine(Color(0xFF546E7A), Offset(20f, 305f), Offset(350f, 305f), strokeWidth = 4f)

            // Pared divisoria vertical
            drawLine(Color(0xFF546E7A), Offset(185f, 60f), Offset(185f, 400f), strokeWidth = 4f)

            // Dibujar los marcadores
            marcadores.forEach { marcador ->
                drawCircle(
                    color = Color(0xFFE53935),
                    radius = marcador.radio,
                    center = marcador.offset
                )
                drawCircle(
                    color = Color.White,
                    radius = marcador.radio,
                    center = marcador.offset,
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }

    // Mostrar información del marcador seleccionado
    marcadorSeleccionado?.let { marcador ->

        AlertDialog(
            onDismissRequest = onCerrar,
            title = {
                Text(text = marcador.titulo)
            },
            text = {
                Text(text = marcador.descripcion)
            },
            confirmButton = {
                TextButton(
                    onClick = onCerrar
                ) {
                    Text(text = "Cerrar")
                }
            }
        )
    }
}

// Vista previa en Android Studio
@Preview(showBackground = true)
@Composable
fun PlanoAmbientePreview() {
    PlanoInteractivo_ComposeTheme {
        PlanoContenido(
            marcadores = listOf(
                Marcador(Offset(100f, 115f), 20f, "Aula A101", "Salón de clases del primer nivel"),
                Marcador(Offset(272f, 115f), 20f, "Aula A102", "Salón de clases del primer nivel"),
                Marcador(Offset(100f, 245f), 20f, "Lab. Cómputo", "Laboratorio de cómputo"),
                Marcador(Offset(272f, 245f), 20f, "Patio Central", "Patio de recreo y descanso"),
                Marcador(Offset(100f, 360f), 20f, "Biblioteca", "Sala de lectura y estudio"),
                Marcador(Offset(272f, 360f), 20f, "Cafetería", "Servicio de alimentos")
            ),
            marcadorSeleccionado = null,
            onMarcadorClick = { },
            onCerrar = { }
        )
    }
}
