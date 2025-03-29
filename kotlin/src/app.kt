package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*

@Composable
fun CompleteApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Display) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            AppTopBar(
                onScreenChanged = { screen = it },
                screen = screen,
                bitmap = bitmap,
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            AppBody(
                screen = screen,
                onScreenChanged = { screen = it },
                bitmap = bitmap,
                onBitmapChanged = { bitmap = it },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    screen: Screen,
    onScreenChanged: (Screen) -> Unit,
    bitmap: Bitmap?,
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    TopAppBar(
        title = { NameCard() },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        onScreenChanged(Screen.Settings)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Save") },
                    onClick = {
                        if (bitmap != null) {
                            writeBitmapToGallery(
                                context = context,
                                bitmap = bitmap,
                                fileName = "mandelbrot",
                            )
                        }
                        expanded = false
                    }
                )
            }
        },
    ) 
}

@Composable
fun AppBody(
    screen: Screen,
    onScreenChanged: (Screen) -> Unit,
    bitmap: Bitmap?,
    onBitmapChanged: (Bitmap?) -> Unit
) {
    var mandelbrotGenerator by remember {
        mutableStateOf<MandelbrotGenerator>(RustMandelbrotGenerator())
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(mandelbrotGenerator) {
        scope.launch {
            onBitmapChanged(null)
            onBitmapChanged(mandelbrotGenerator.genBitmap())
        }
    }

    when (screen) {
        is Screen.Display -> {
            DisplayScreen(
                mandelbrotBitmap = bitmap,
                mandelbrotGenerator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = { mandelbrotGenerator = it },
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                generator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = {
                    mandelbrotGenerator = it
                    onScreenChanged(Screen.Display)
                },
            )
        }
    }
}

sealed class Screen {
    object Display : Screen()
    object Settings : Screen()
}
