package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*

@Composable
fun App() {
    var mandelbrotGenerator by remember {
        mutableStateOf<MandelbrotGenerator>(KotlinMandelbrotGenerator())
    }

    var mandelbrotBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            mandelbrotBitmap = mandelbrotGenerator.genBitmap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        if (mandelbrotBitmap != null) {
            Image(
                bitmap = mandelbrotBitmap!!.asImageBitmap(),
                contentDescription = "Mandelbrot Set",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
