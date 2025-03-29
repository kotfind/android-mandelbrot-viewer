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
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlinx.coroutines.*

@Composable
fun CompleteApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Display) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var mandelbrotGenerator by remember {
        mutableStateOf<MandelbrotGenerator>(RustMandelbrotGenerator())
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            AppTopBar(
                onScreenChanged = { screen = it },
                screen = screen,
                bitmap = bitmap,
                mandelbrotGenerator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = { mandelbrotGenerator = it },
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
                mandelbrotGenerator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = { mandelbrotGenerator = it },
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
    mandelbrotGenerator: MandelbrotGenerator,
    onMandelbrotGeneratorChanged: (MandelbrotGenerator) -> Unit,
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

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Reset Transform") },
                    onClick = {
                        val gen = MandelbrotGenerator.clone(mandelbrotGenerator)
                        gen.centerX = 0.0
                        gen.centerY = 0.0
                        gen.range = 3.0
                        onMandelbrotGeneratorChanged(gen)
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Reset All") },
                    onClick = {
                        onMandelbrotGeneratorChanged(RustMandelbrotGenerator())
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
    onBitmapChanged: (Bitmap?) -> Unit,
    mandelbrotGenerator: MandelbrotGenerator,
    onMandelbrotGeneratorChanged: (MandelbrotGenerator) -> Unit,

) {
    var evalTime by remember { mutableStateOf<Duration>(Duration.ZERO) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(mandelbrotGenerator) {
        scope.launch {
            onBitmapChanged(null)
            evalTime = measureTime {
                onBitmapChanged(mandelbrotGenerator.genBitmap())
            }
        }
    }

    when (screen) {
        is Screen.Display -> {
            DisplayScreen(
                mandelbrotBitmap = bitmap,
                mandelbrotGenerator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = onMandelbrotGeneratorChanged,
                evalTime = evalTime,
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                generator = mandelbrotGenerator,
                onMandelbrotGeneratorChanged = {
                    onMandelbrotGeneratorChanged(it)
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
