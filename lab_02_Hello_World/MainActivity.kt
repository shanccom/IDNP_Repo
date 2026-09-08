package com.example.helloworldcompose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.layout.Row

val Background = Color(0xFFF5F5F3)
val Surface = Color(0xFFFFFEFC)
val Black = Color(0xFF171717)
val Gray = Color(0xFF777777)
val Border = Color(0xFF202020)

val Mint = Color(0xFFA8D5BA)
val MintLight = Color(0xFFE7F3EC)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegistroLibro()
        }
    }
}

@Composable
fun RegistroLibro() {

    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var paginas by remember { mutableStateOf("") }
    var registro by remember { mutableStateOf("")}

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        )
    ) {

        Text(
            text = "BOOK QUEST",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = "Registra tu aventura",
            fontSize = 16.sp,
            color = Gray
        )

        OutlinedTextField(
            value = titulo,
            onValueChange = {
                titulo = it
            },
            label = {
                Text("Título del libro")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Black,
                unfocusedBorderColor = Border,
                focusedLabelColor = Black,
                unfocusedLabelColor = Gray,
                focusedTextColor = Black,
                unfocusedTextColor = Black
            )
        )

        OutlinedTextField(
            value = autor,
            onValueChange = {
                autor = it
            },
            label = {
                Text("Autor")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Black,
                unfocusedBorderColor = Border,
                focusedLabelColor = Black,
                unfocusedLabelColor = Gray,
                focusedTextColor = Black,
                unfocusedTextColor = Black
            )
        )

        OutlinedTextField(
            value = paginas,
            onValueChange = {
                paginas = it
            },
            label = {
                Text("Páginas leídas")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = Black,
                unfocusedBorderColor = Border,
                focusedLabelColor = Black,
                unfocusedLabelColor = Gray,
                focusedTextColor = Black,
                unfocusedTextColor = Black
            )
        )


        Button(
            onClick = {
                GuardarDatos(context = context, titulo = titulo, autor = autor, paginas = paginas)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Mint,
                contentColor = Black
            )
        ) {
            Text(
                text = "GUARDAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                registro = LeerDatos(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Surface,
                contentColor = Black
            )
        ) {
            Text(
                text = "VER REGISTRO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (registro.isNotEmpty()) {

            Text(
                text = "LIBRO REGISTRADO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black

            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Mint,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {

                Text(
                    text = "TÍTULO",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    color = Black
                )

                Text(
                    text = "AUTOR",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    color = Black
                )

                Text(
                    text = "PÁGINAS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = Black
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {

                Text(
                    text = titulo,
                    modifier = Modifier.weight(1.5f),
                    color = Black
                )

                Text(
                    text = autor,
                    modifier = Modifier.weight(1.5f),
                    color = Black
                )

                Text(
                    text = paginas,
                    modifier = Modifier.weight(1f),
                    color = Black
                )
            }
        }
    }
}

fun GuardarDatos(
    context: Context,
    titulo: String,
    autor: String,
    paginas: String,
){
    val datos = "Titulo: $titulo Autor: $autor paginas: $paginas".trimIndent()

    context.openFileOutput(
        "datos.txt",
        Context.MODE_PRIVATE
    ).use {
        archivo -> archivo.write(datos.toByteArray())
    }
}

fun LeerDatos(context: Context): String {

    val datos = context.openFileInput("datos.txt")
        .bufferedReader()
        .use { it.readText() }

    Log.d("REGISTRO_LIBRO", datos)
    return datos
}
