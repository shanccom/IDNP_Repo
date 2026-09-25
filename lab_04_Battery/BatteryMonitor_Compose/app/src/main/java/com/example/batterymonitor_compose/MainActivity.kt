package com.example.batterymonitor_compose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.batterymonitor_compose.ui.theme.BatteryMonitor_ComposeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ACCION_ACTUALIZAR_BATERIA = "com.tuapp.ACTUALIZAR_BATERIA"

private val COLOR_CARGA_ALTA = Color(0xFF2E7D32)
private val COLOR_CARGA_MEDIA = Color(0xFFF9A825)
private val COLOR_CARGA_BAJA = Color(0xFFC62828)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BatteryMonitor_ComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    BatteryScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryScreen(modifier: Modifier = Modifier) {

    // Paso 2: Variable de estado
    var porcentaje by remember {
        mutableIntStateOf(0)
    }

    var estaCargando by remember {
        mutableStateOf(false)
    }

    var ultimaActualizacion by remember {
        mutableStateOf<String?>(null)
    }

    // Paso 3: Obtener el contexto
    val context = LocalContext.current

    // Pasos 4, 5 y 6
    DisposableEffect(Unit) {

        val receiverAutomatico = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                leerEstadoBateria(intent)?.let { estado ->
                    porcentaje = estado.porcentaje
                    estaCargando = estado.estaCargando
                }
            }
        }

        val filtroAutomatico = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }

        // Paso 5: Registrar el receiver
        ContextCompat.registerReceiver(
            context,
            receiverAutomatico,
            filtroAutomatico,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        Log.d(
            "BatteryScreen",
            "Receiver automático registrado"
        )

        // Paso 6: Desregistrar el receiver
        onDispose {

            context.unregisterReceiver(receiverAutomatico)

            Log.d(
                "BatteryScreen",
                "Receiver automático desregistrado"
            )
        }
    }

    DisposableEffect(Unit) {

        val receiverManual = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                Log.d(
                    "BatteryScreen",
                    "Actualización manual recibida"
                )

                if (context != null) {
                    estadoActualBateria(context)?.let { estado ->
                        porcentaje = estado.porcentaje
                        estaCargando = estado.estaCargando
                    }

                    ultimaActualizacion = horaActual(context)
                }
            }
        }

        val filtroManual = IntentFilter().apply {
            addAction(ACCION_ACTUALIZAR_BATERIA)
        }

        ContextCompat.registerReceiver(
            context,
            receiverManual,
            filtroManual,
            ContextCompat.RECEIVER_EXPORTED
        )

        Log.d(
            "BatteryScreen",
            "Receiver manual registrado"
        )

        onDispose {

            context.unregisterReceiver(receiverManual)

            Log.d(
                "BatteryScreen",
                "Receiver manual desregistrado"
            )
        }
    }

    val proporcion = animateFloatAsState(
        targetValue = porcentaje / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "cargaBateria"
    )

    val colorCarga = colorSegunCarga(porcentaje)

    // Paso 7: Mostrar porcentaje
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconoBateria(
            carga = proporcion.value,
            color = colorCarga
        )

        Text(
            text = "$porcentaje%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = colorCarga,
            modifier = Modifier.padding(top = 24.dp)
        )

        IndicadorCarga(
            estaCargando = estaCargando
        )

        Text(
            text = "Nivel de batería actual",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { enviarActualizacionManual(context) },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorCarga,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "Actualizar manualmente",
                fontWeight = FontWeight.SemiBold
            )
        }

        ultimaActualizacion?.let { momento ->
            Text(
                text = "Actualizado a las $momento",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

private fun horaActual(context: Context): String {

    val formato24Horas = DateFormat.is24HourFormat(context)

    val patron = if (formato24Horas) "HH:mm:ss" else "hh:mm:ss a"

    return SimpleDateFormat(patron, Locale.getDefault()).format(Date())
}

private fun colorSegunCarga(porcentaje: Int): Color {
    return when {
        porcentaje >= 80 -> COLOR_CARGA_ALTA
        porcentaje >= 40 -> COLOR_CARGA_MEDIA
        else -> COLOR_CARGA_BAJA
    }
}

@Composable
private fun IndicadorCarga(estaCargando: Boolean) {

    val texto = if (estaCargando) "Cargando" else "Sin carga"

    val colorFondo = if (estaCargando) {
        COLOR_CARGA_ALTA
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val colorTexto = if (estaCargando) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .background(
                color = colorFondo,
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorTexto
        )
    }
}

@Composable
private fun IconoBateria(
    carga: Float,
    color: Color
) {
    val colorContenedor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .aspectRatio(3.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.72f)
                    .background(
                        color = colorContenedor,
                        shape = RoundedCornerShape(percent = 40)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .fillMaxWidth(carga.coerceIn(0.06f, 1f))
                            .background(
                                color = color,
                                shape = RoundedCornerShape(percent = 50)
                            )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight(0.28f)
                    .background(
                        color = colorContenedor,
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
        }
    }
}

private data class EstadoBateria(
    val porcentaje: Int,
    val estaCargando: Boolean
)

private fun leerEstadoBateria(intent: Intent?): EstadoBateria? {

    if (intent == null) {
        return null
    }

    val nivel = intent.getIntExtra(
        BatteryManager.EXTRA_LEVEL,
        -1
    )

    val escala = intent.getIntExtra(
        BatteryManager.EXTRA_SCALE,
        -1
    )

    if (nivel == -1 || escala <= 0) {
        return null
    }

    val status = intent.getIntExtra(
        BatteryManager.EXTRA_STATUS,
        -1
    )

    return EstadoBateria(
        porcentaje = (nivel * 100) / escala,
        estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    )
}

private fun estadoActualBateria(context: Context): EstadoBateria? {

    val batteryManager = context.getSystemService(BatteryManager::class.java)

    val capacidad = batteryManager?.getIntProperty(
        BatteryManager.BATTERY_PROPERTY_CAPACITY
    )

    if (capacidad != null && capacidad in 0..100) {
        return EstadoBateria(
            porcentaje = capacidad,
            estaCargando = batteryManager.isCharging()
        )
    }

    val estado = ContextCompat.registerReceiver(
        context,
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ContextCompat.RECEIVER_NOT_EXPORTED
    )

    return leerEstadoBateria(estado)
}

private fun enviarActualizacionManual(context: Context) {

    val intentPersonalizado = Intent(ACCION_ACTUALIZAR_BATERIA)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intentPersonalizado,
        PendingIntent.FLAG_UPDATE_CURRENT or
            PendingIntent.FLAG_IMMUTABLE
    )

    pendingIntent.send()
}