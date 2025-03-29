package org.kotfind.mandelbrot_viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
        title = {
            Column {
                Text(
                    text = "Mandelbrot Viewer",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "by kotfind",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
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
                        expanded = false
                        onScreenChanged(Screen.Settings)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Save") },
                    onClick = {
                        expanded = false
                        if (bitmap != null) {
                            writeBitmapToGallery(
                                context = context,
                                bitmap = bitmap,
                                fileName = "mandelbrot",
                            )
                            Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Image is loading.\nCannot save.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Reset Transform") },
                    onClick = {
                        expanded = false

                        val gen = MandelbrotGenerator.clone(mandelbrotGenerator)
                        gen.centerX = 0.0
                        gen.centerY = 0.0
                        gen.range = 3.0

                        onScreenChanged(Screen.Display)
                        onMandelbrotGeneratorChanged(gen)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Reset All") },
                    onClick = {
                        expanded = false

                        onScreenChanged(Screen.Display)
                        onMandelbrotGeneratorChanged(RustMandelbrotGenerator())
                    }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Export Settings") },
                    onClick = {
                        expanded = false

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "mandelbrot options",
                            mandelbrotGenerator.toOptionsString()
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied settings to clipboard.", Toast.LENGTH_SHORT).show()
                    },
                )

                DropdownMenuItem(
                    text = { Text("Import Settings") },
                    onClick = {
                        expanded = false

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)
                        if (text == null) {
                            Toast.makeText(
                                context,
                                "Failed to read text from clipboard.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@DropdownMenuItem
                        }

                        val gen = MandelbrotGenerator.clone(mandelbrotGenerator)

                        val optionRegex = Regex("""^\s*(\w+)\s*=\s*(.*)\s*$""", RegexOption.DOT_MATCHES_ALL)

                        val matchedOptions = mutableSetOf<String>()

                        for (line in text.lines()) {
                            val matchRes = optionRegex.matchEntire(line)
                            if (matchRes == null) {
                                continue
                            }

                            val optionName = matchRes.groupValues[1]
                            val optionValue = matchRes.groupValues[2]

                            when (optionName) {
                                "centerX" -> {
                                    val v =  optionValue.toDoubleOrNull()
                                    if (v != null) {
                                        matchedOptions.add(optionName)
                                        gen.centerX = v
                                    }
                                }

                                "centerY" -> {
                                    val v =  optionValue.toDoubleOrNull()
                                    if (v != null) {
                                        matchedOptions.add(optionName)
                                        gen.centerY = v
                                    }
                                }

                                "range" -> {
                                    val v =  optionValue.toDoubleOrNull()
                                    if (v != null && v > 0) {
                                        matchedOptions.add(optionName)
                                        gen.range = v
                                    }
                                }

                                "bitmapSize" -> {
                                    val v = optionValue.toIntOrNull()
                                    if (v != null && v >= 1) {
                                        matchedOptions.add(optionName)
                                        gen.bitmapSize = v
                                    }
                                }

                                "maxIter" -> {
                                    val v = optionValue.toIntOrNull()
                                    if (v != null && v >= 1) {
                                        matchedOptions.add(optionName)
                                        gen.maxIter = v
                                    }
                                }

                                else -> {}

                                // NOTE: 'type' is ignored
                            }
                        }

                        onScreenChanged(Screen.Display)
                        onMandelbrotGeneratorChanged(gen)
                        
                        Toast.makeText(
                            context,
                            "Imported options: ${matchedOptions.joinToString(", ")}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
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

    var mandelbrotGeneratorCoroutine: Job? = null
    val scope = rememberCoroutineScope()
    LaunchedEffect(mandelbrotGenerator) {
        mandelbrotGeneratorCoroutine?.cancel()
        mandelbrotGeneratorCoroutine = scope.launch {
            onBitmapChanged(null)
            evalTime = measureTime {
                onBitmapChanged(mandelbrotGenerator.genBitmap())
            }
        }
    }

    BackPressHandler(
        screen = screen,
        onScreenChanged = onScreenChanged
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackPressHandler(
    screen: Screen,
    onScreenChanged: (Screen) -> Unit,
) {
    val activity = LocalContext.current as Activity

    BackHandler(enabled = true) {
        when (screen) {
            is Screen.Display -> {
                activity.finish()
            }

            is Screen.Settings -> {
                onScreenChanged(Screen.Display)
            }
        }
    }
}

sealed class Screen {
    object Display : Screen()
    object Settings : Screen()
}
