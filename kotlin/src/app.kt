package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import kotlinx.coroutines.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

@Composable
fun CompleteApp() {
    var screen by remember { mutableStateOf<Screen>(Screen.Display) }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            AppTopBar(
                onScreenChanged = { screen = it },
                screen = screen,
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
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    screen: Screen,
    onScreenChanged: (Screen) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    
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
) {
    var mandelbrotGenerator by remember {
        mutableStateOf<MandelbrotGenerator>(RustMandelbrotGenerator())
    }

    var mandelbrotBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(mandelbrotGenerator) {
        scope.launch {
            mandelbrotBitmap = null
            mandelbrotBitmap = mandelbrotGenerator.genBitmap()
        }
    }

    when (screen) {
        is Screen.Display -> {
            DisplayScreen(mandelbrotBitmap)
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
