package com.example.lab_08_persistenciaroom_compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.example.lab_08_persistenciaroom_compose.ui.theme.Lab_08_PersistenciaRoom_ComposeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Entity
data class EdificioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val categoria: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EdificioEntity::class,
            parentColumns = ["id"],
            childColumns = ["edificioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AmbienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val edificioId: Int,
    val nombre: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AmbienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["ambienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ambienteId: Int,
    val texto: String
)

data class EdificioConAmbientes(
    @Embedded
    val edificio: EdificioEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "edificioId"
    )
    val ambientes: List<AmbienteEntity>
)

data class AmbienteConNotas(
    @Embedded
    val ambiente: AmbienteEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "ambienteId"
    )
    val notas: List<NotaEntity>
)

@Dao
interface EdificioDao {

    @Insert
    suspend fun insertarEdificio(
        edificio: EdificioEntity
    ): Long

    @Insert
    suspend fun insertarAmbiente(
        ambiente: AmbienteEntity
    )

    @Insert
    suspend fun insertarNota(
        nota: NotaEntity
    ): Long

    @Update
    suspend fun actualizarEdificio(
        edificio: EdificioEntity
    )

    @Update
    suspend fun actualizarAmbiente(
        ambiente: AmbienteEntity
    )

    @Update
    suspend fun actualizarNota(
        nota: NotaEntity
    )

    @Delete
    suspend fun eliminarEdificio(
        edificio: EdificioEntity
    )

    @Delete
    suspend fun eliminarAmbiente(
        ambiente: AmbienteEntity
    )

    @Delete
    suspend fun eliminarNota(
        nota: NotaEntity
    )

    @Query("SELECT * FROM EdificioEntity")
    suspend fun obtenerTodos(): List<EdificioEntity>

    @Query("SELECT * FROM AmbienteEntity")
    suspend fun obtenerTodosLosAmbientes(): List<AmbienteEntity>

    @Transaction
    @Query("SELECT * FROM EdificioEntity")
    suspend fun obtenerConAmbientes():
            List<EdificioConAmbientes>

    @Transaction
    @Query("SELECT * FROM AmbienteEntity")
    suspend fun obtenerAmbientesConNotas():
            List<AmbienteConNotas>
}

@Database(
    entities = [
        EdificioEntity::class,
        AmbienteEntity::class,
        NotaEntity::class
    ],
    version = 1,
    exportSchema = false
)


abstract class AppDatabase : RoomDatabase() {

    abstract fun edificioDao(): EdificioDao
}

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun obtener(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            ).fallbackToDestructiveMigration(dropAllTables = true).build().also {
                INSTANCE = it
            }
        }
    }
}

class EdificiosViewModel(
    private val dao: EdificioDao
) : ViewModel() {

    private val _edificios =
        MutableStateFlow<List<EdificioConAmbientes>>(emptyList())

    val edificios: StateFlow<List<EdificioConAmbientes>> =
        _edificios

    init {
        viewModelScope.launch {
            insertarDatosIniciales()
            cargarEdificios()
        }
    }

    private suspend fun insertarDatosIniciales() {
        if (dao.obtenerTodos().isEmpty()) {

            val id1 = dao.insertarEdificio(
                EdificioEntity(
                    nombre = "Biblioteca Central",
                    categoria = "Servicios"
                )
            )

            dao.insertarAmbiente(
                AmbienteEntity(
                    edificioId = id1.toInt(),
                    nombre = "Sala de lectura"
                )
            )

            dao.insertarAmbiente(
                AmbienteEntity(
                    edificioId = id1.toInt(),
                    nombre = "Hemeroteca"
                )
            )

            val id2 = dao.insertarEdificio(
                EdificioEntity(
                    nombre = "Facultad de Ingeniería",
                    categoria = "Académico"
                )
            )

            dao.insertarAmbiente(
                AmbienteEntity(
                    edificioId = id2.toInt(),
                    nombre = "Laboratorio de cómputo"
                )
            )
        }
    }

    suspend fun cargarEdificios() {
        _edificios.value = dao.obtenerConAmbientes()
    }

    fun insertarEdificio(nombre: String, categoria: String) {
        viewModelScope.launch {
            dao.insertarEdificio(
                EdificioEntity(
                    nombre = nombre,
                    categoria = categoria
                )
            )
            cargarEdificios()
        }
    }

    fun insertarAmbiente(edificioId: Int, nombre: String) {
        viewModelScope.launch {
            dao.insertarAmbiente(
                AmbienteEntity(
                    edificioId = edificioId,
                    nombre = nombre
                )
            )
            cargarEdificios()
        }
    }

    fun actualizarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.actualizarEdificio(edificio)
            cargarEdificios()
        }
    }

    fun eliminarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.eliminarEdificio(edificio)
            cargarEdificios()
        }
    }

    fun actualizarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.actualizarAmbiente(ambiente)
            cargarEdificios()
        }
    }

    fun eliminarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.eliminarAmbiente(ambiente)
            cargarEdificios()
        }
    }
}

class EdificiosViewModelFactory(
    private val dao: EdificioDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(
                EdificiosViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return EdificiosViewModel(dao) as T
        }

        throw IllegalArgumentException(
            "Clase ViewModel desconocida"
        )
    }
}

@Composable
fun ListaEdificiosConAmbientes(
    viewModel: EdificiosViewModel
) {
    val edificios by viewModel.edificios.collectAsState()

    var edificioAEditar by remember { mutableStateOf<EdificioEntity?>(null) }
    var edificioAEliminar by remember { mutableStateOf<EdificioEntity?>(null) }
    var ambienteAEditar by remember { mutableStateOf<AmbienteEntity?>(null) }
    var ambienteAEliminar by remember { mutableStateOf<AmbienteEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Encabezado estilizado minimalista
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GESTIÓN DE CAMPUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Edificios y Ambientes",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${edificios.size} reg.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Persistencia Room en SQLite con relaciones 1:N",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (edificios.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay registros disponibles",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = edificios,
                    key = { it.edificio.id }
                ) { item ->

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Fila superior: Categoría, ID y Acciones del Edificio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = item.edificio.categoria.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "#${item.edificio.id}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Botones de Acción para Edificio (Actualizar y Eliminar)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { edificioAEditar = item.edificio },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Edificio",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { edificioAEliminar = item.edificio },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar Edificio",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.edificio.nombre,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Subsección de Ambientes
                            Text(
                                text = "AMBIENTES (${item.ambientes.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (item.ambientes.isEmpty()) {
                                Text(
                                    text = "Sin ambientes vinculados",
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item.ambientes.forEach { ambiente ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            border = BorderStroke(
                                                0.8.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = ambiente.nombre,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                // Botones de Acción para Ambiente (Actualizar y Eliminar)
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = { ambienteAEditar = ambiente },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Editar Ambiente",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = { ambienteAEliminar = ambiente },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar Ambiente",
                                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Edición de Edificio
    edificioAEditar?.let { edificio ->
        var editNombre by remember(edificio) { mutableStateOf(edificio.nombre) }
        var editCategoria by remember(edificio) { mutableStateOf(edificio.categoria) }

        AlertDialog(
            onDismissRequest = { edificioAEditar = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Actualizar Edificio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre del edificio") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editCategoria,
                        onValueChange = { editCategoria = it },
                        label = { Text("Categoría") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNombre.isNotBlank()) {
                            viewModel.actualizarEdificio(
                                edificio.copy(
                                    nombre = editNombre.trim(),
                                    categoria = editCategoria.trim()
                                )
                            )
                            edificioAEditar = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { edificioAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmación de Eliminación de Edificio
    edificioAEliminar?.let { edificio ->
        AlertDialog(
            onDismissRequest = { edificioAEliminar = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "¿Eliminar edificio?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Se eliminará permanentemente \"${edificio.nombre}\" y todos sus ambientes asociados en la base de datos.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarEdificio(edificio)
                        edificioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { edificioAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Edición de Ambiente
    ambienteAEditar?.let { ambiente ->
        var editNombre by remember(ambiente) { mutableStateOf(ambiente.nombre) }

        AlertDialog(
            onDismissRequest = { ambienteAEditar = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Actualizar Ambiente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = editNombre,
                    onValueChange = { editNombre = it },
                    label = { Text("Nombre del ambiente") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNombre.isNotBlank()) {
                            viewModel.actualizarAmbiente(
                                ambiente.copy(nombre = editNombre.trim())
                            )
                            ambienteAEditar = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ambienteAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmación de Eliminación de Ambiente
    ambienteAEliminar?.let { ambiente ->
        AlertDialog(
            onDismissRequest = { ambienteAEliminar = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "¿Eliminar ambiente?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar permanentemente el ambiente \"${ambiente.nombre}\"?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarAmbiente(ambiente)
                        ambienteAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ambienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.obtener(
            applicationContext
        )

        val dao = database.edificioDao()

        val factory = EdificiosViewModelFactory(dao)

        setContent {
            Lab_08_PersistenciaRoom_ComposeTheme {
                val viewModel: EdificiosViewModel = viewModel(
                    factory = factory
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListaEdificiosConAmbientes(viewModel)
                }
            }
        }
    }
}
