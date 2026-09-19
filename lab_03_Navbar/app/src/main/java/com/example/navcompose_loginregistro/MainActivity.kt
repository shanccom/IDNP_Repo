package com.example.navcompose_loginregistro

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {

                composable("login") {

                    LoginScreen(

                        onLoginExitoso = { usuario ->
                            navController.navigate("home/$usuario")
                        },

                        onIrARegistro = {
                            navController.navigate("registro")
                        }
                    )
                }

                composable("registro") {

                    RegistroScreen(

                        onRegistroExitoso = {
                            Toast.makeText(
                                this@MainActivity,
                                "Cuenta creada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        },

                        onCancelar = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    "home/{usuario}",
                    arguments = listOf(navArgument("usuario") { type = NavType.StringType })
                ) { backStackEntry ->
                    val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
                    HomeScreen(usuario = usuario)
                }
            }
        }
    }
}

val cuentasRegistradas = mutableStateListOf(
    "admin" to "1234"
)

fun guardarCuenta(context: Context, usuario: String, password: String) {
    val linea = "$usuario,$password\n"
    context.openFileOutput("cuentas.txt", Context.MODE_APPEND).use {
        archivo -> archivo.write(linea.toByteArray())
    }
}

fun buscarCuenta(context: Context, usuario: String, password: String): Boolean {
    return try {
        val contenido = context.openFileInput("cuentas.txt")
            .bufferedReader()
            .use { it.readText() }

        contenido.lineSequence().any { linea ->
            linea.trim() == "$usuario,$password"
        }
    } catch (e: Exception) {
        false
    }
}

@Composable
fun LoginScreen(
    onLoginExitoso: (String) -> Unit,
    onIrARegistro: () -> Unit
) {

    var usuario by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf("")
    }

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
            text = "BIENVENIDO",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = "Inicia sesión para continuar",
            fontSize = 16.sp,
            color = Gray
        )

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
            },
            label = {
                Text("Usuario")
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
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Contraseña")
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
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        if (mensajeError.isNotEmpty()) {

            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {

                when {
                    usuario.isBlank() && password.isBlank() -> {
                        mensajeError = "Completa todos los campos"
                    }
                    usuario.isBlank() -> {
                        mensajeError = "El campo usuario es obligatorio"
                    }
                    password.isBlank() -> {
                        mensajeError = "El campo contraseña es obligatorio"
                    }
                    else -> {
                        val coincide = buscarCuenta(
                            context, usuario, password
                        )

                        if (coincide) {

                            mensajeError = ""

                            onLoginExitoso(usuario)

                        } else {

                            mensajeError =
                                "Cuenta no encontrada"
                        }
                    }
                }
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
                text = "INGRESAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onIrARegistro,
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
                text = "CREAR CUENTA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onCancelar: () -> Unit
) {

    var usuario by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf("")
    }

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
            text = "CREAR CUENTA",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = "Registra tus datos",
            fontSize = 16.sp,
            color = Gray
        )

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
            },
            label = {
                Text("Nuevo usuario")
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
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Nueva contraseña")
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
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        if (mensajeError.isNotEmpty()) {

            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {

                when {
                    usuario.isBlank() && password.isBlank() -> {
                        mensajeError = "Completa todos los campos"
                    }
                    usuario.isBlank() -> {
                        mensajeError = "El campo usuario es obligatorio"
                    }
                    password.isBlank() -> {
                        mensajeError = "El campo contraseña es obligatorio"
                    }
                    else -> {
                        cuentasRegistradas.add(
                            usuario to password
                        )

                        guardarCuenta(context, usuario, password)

                        mensajeError = ""

                        onRegistroExitoso()
                    }
                }
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
                text = "ACEPTAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onCancelar,
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
                text = "CANCELAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeScreen(usuario: String) {

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
            text = "Bienvenido $usuario",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = "Iniciaste sesión correctamente",
            fontSize = 16.sp,
            color = Gray
        )
    }
}