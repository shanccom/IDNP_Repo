package com.example.lab08_listafiltrable_compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab08_listafiltrable_compose.ui.theme.Lab08_ListaFiltrable_ComposeTheme
import java.io.File

private val FondoApp = Color(0xFFF7F4EE)
private val FondoTarjeta = Color(0xFFFFFFFF)
private val BordeSuave = Color(0xFFE3DED3)
private val TextoPrincipal = Color(0xFF23261F)
private val TextoSecundario = Color(0xFF77776E)
private val VerdeCampus = Color(0xFF2F5D46)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Lab08_ListaFiltrable_ComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FondoApp
                ) {
                    ListaEdificios()
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ListaEdificiosPreview() {
    Lab08_ListaFiltrable_ComposeTheme {
        ListaEdificiosContenido(
            edificios = parsearEdificios(
                """
                1,Biblioteca Central UNSA,Biblioteca,Préstamo de libros y salas de estudio
                2,Facultad de Ingeniería de Sistemas,Académico,Aulas y laboratorios de computación
                """.trimIndent()
            )
        )
    }
}


data class Edificio(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String
)

private const val ARCHIVO_EDIFICIOS = "edificios.txt"

fun parsearEdificios(contenido: String): List<Edificio> {
    return contenido.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { linea ->
            val partes = linea.split(",", limit = 4).map { it.trim() }
            if (partes.size < 4) return@mapNotNull null
            val id = partes[0].toIntOrNull() ?: return@mapNotNull null
            if (partes[1].isEmpty()) return@mapNotNull null
            Edificio(id, partes[1], partes[2], partes[3])
        }
        .toList()
}

fun cargarEdificios(context: Context): List<Edificio> {
    return try {
        val archivo = File(context.filesDir, ARCHIVO_EDIFICIOS)
        if (!archivo.exists()) {
            context.assets.open(ARCHIVO_EDIFICIOS).use { entrada ->
                archivo.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            }
        }
        val contenido = context.openFileInput(ARCHIVO_EDIFICIOS)
            .bufferedReader()
            .use { it.readText() }
        parsearEdificios(contenido)
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun ListaEdificios() {
    val context = LocalContext.current
    val edificios = remember { cargarEdificios(context) }
    ListaEdificiosContenido(edificios)
}

private const val TODAS_LAS_CATEGORIAS = "Todas"

fun filtrarEdificios(
    edificios: List<Edificio>,
    nombre: String,
    categoria: String
): List<Edificio> {
    val nombreBuscado = nombre.trim()
    return edificios.filter { edificio ->
        val coincideNombre = nombreBuscado.isEmpty() ||
            edificio.nombre.contains(nombreBuscado, ignoreCase = true)
        val coincideCategoria = categoria == TODAS_LAS_CATEGORIAS ||
            edificio.categoria.equals(categoria, ignoreCase = true)
        coincideNombre && coincideCategoria
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FondoTarjeta,
    unfocusedContainerColor = FondoTarjeta,
    focusedBorderColor = VerdeCampus,
    unfocusedBorderColor = BordeSuave,
    focusedLabelColor = VerdeCampus,
    unfocusedLabelColor = TextoSecundario,
    focusedTextColor = TextoPrincipal,
    unfocusedTextColor = TextoPrincipal,
    cursorColor = VerdeCampus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroCategoria(
    categorias: List<String>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccionada,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Categoría") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampo()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        onSeleccionar(categoria)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun ListaEdificiosContenido(edificios: List<Edificio>) {
    var texto by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(TODAS_LAS_CATEGORIAS) }

    val categorias = remember(edificios) {
        listOf(TODAS_LAS_CATEGORIAS) +
            edificios.map { it.categoria }.distinct().sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nombre") },
                placeholder = { Text("Buscar edificio") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = coloresCampo()
            )
            FiltroCategoria(
                categorias = categorias,
                seleccionada = categoriaSeleccionada,
                onSeleccionar = { categoriaSeleccionada = it },
                modifier = Modifier.width(158.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val edificiosFiltrados = filtrarEdificios(
            edificios = edificios,
            nombre = texto,
            categoria = categoriaSeleccionada
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(edificiosFiltrados, key = { it.id }) { edificio ->
                TarjetaLugar(edificio)
            }
        }
    }
}

private data class EstiloCategoria(
    val icono: ImageVector,
    val colorFondo: Color,
    val colorIcono: Color
)

private fun estiloCategoria(categoria: String): EstiloCategoria = when (categoria.lowercase()) {
    "académico", "academico" -> EstiloCategoria(
        Icons.Default.School, Color(0xFFE3F2FD), Color(0xFF1565C0)
    )
    "biblioteca" -> EstiloCategoria(
        Icons.Default.LocalLibrary, Color(0xFFE8EAF6), Color(0xFF283593)
    )
    "tecnología", "tecnologia" -> EstiloCategoria(
        Icons.Default.Computer, Color(0xFFE0F2F1), Color(0xFF00695C)
    )
    "servicios" -> EstiloCategoria(
        Icons.Default.Restaurant, Color(0xFFFFF3E0), Color(0xFFEF6C00)
    )
    "deportes" -> EstiloCategoria(
        Icons.Default.SportsSoccer, Color(0xFFE8F5E9), Color(0xFF2E7D32)
    )
    "eventos" -> EstiloCategoria(
        Icons.Default.Event, Color(0xFFF3E5F5), Color(0xFF6A1B9A)
    )
    "administrativo" -> EstiloCategoria(
        Icons.Default.Business, Color(0xFFECEFF1), Color(0xFF37474F)
    )
    else -> EstiloCategoria(
        Icons.Default.Place, Color(0xFFF5F5F5), Color.Gray
    )
}

// TarjetaLugar adaptada del Laboratorio 06: conserva el bloque visual,
// el nombre, la descripción y la etiqueta; usa el ícono como imagen.
@Composable
fun TarjetaLugar(edificio: Edificio) {
    val estilo = estiloCategoria(edificio.categoria)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FondoTarjeta,
        border = BorderStroke(1.dp, BordeSuave),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(estilo.colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = estilo.icono,
                    contentDescription = "Categoría ${edificio.categoria}",
                    tint = estilo.colorIcono,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = edificio.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = edificio.descripcion,
                    fontSize = 11.sp,
                    color = TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = estilo.colorIcono.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = edificio.categoria,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estilo.colorIcono,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}


