package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DisplayScreen(
    mandelbrotBitmap: Bitmap?,
    mandelbrotGenerator: MandelbrotGenerator,
    onMandelbrotGeneratorChanged: (MandelbrotGenerator) -> Unit,
) {
    if (mandelbrotBitmap == null) {
        Text(
            text = "Loading...",
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        return
    }

    val imageSize by remember { mutableStateOf(IntSize(100, 100)) }

    Image(
        bitmap = mandelbrotBitmap.asImageBitmap(),
        contentDescription = "Mandelbrot Set",
        modifier = Modifier
            .fillMaxSize()
            .transformable(rememberTransformableState { deltaZoom, deltaOffset, _deltaRotation ->
                // TODO: use clone here
                val oldGen = mandelbrotGenerator
                val gen = MandelbrotGenerator.fromType(oldGen.getType())
                gen.range = oldGen.range / deltaZoom
                gen.centerX = oldGen.centerX - deltaOffset.x * gen.range / imageSize.width
                gen.centerY = oldGen.centerY - deltaOffset.y * gen.range / imageSize.height
                gen.bitmapSize = oldGen.bitmapSize
                gen.maxIter = oldGen.maxIter
                onMandelbrotGeneratorChanged(gen)
            })
    )
}
