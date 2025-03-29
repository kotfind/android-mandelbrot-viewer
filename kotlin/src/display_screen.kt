package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DisplayScreen(
    mandelbrotBitmap: Bitmap?,
) {
    if (mandelbrotBitmap != null) {
        Image(
            bitmap = mandelbrotBitmap.asImageBitmap(),
            contentDescription = "Mandelbrot Set",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Text(
            text = "Loading...",
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
