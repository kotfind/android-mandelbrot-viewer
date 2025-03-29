package org.kotfind.mandelbrot_viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import kotlin.math.min
import kotlin.time.Duration

@Composable
fun DisplayScreen(
    mandelbrotBitmap: Bitmap?,
    mandelbrotGenerator: MandelbrotGenerator,
    onMandelbrotGeneratorChanged: (MandelbrotGenerator) -> Unit,
    evalTime: Duration,
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

    var imageSize by remember { mutableStateOf(IntSize(100, 100)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(rememberTransformableState { deltaZoom, deltaOffset, _deltaRotation ->
                val gen = MandelbrotGenerator.clone(mandelbrotGenerator)
                gen.range /= deltaZoom

                val scale = gen.range / min(imageSize.width, imageSize.height)
                gen.centerX -= deltaOffset.x * scale
                gen.centerY -= deltaOffset.y * scale

                onMandelbrotGeneratorChanged(gen)
            })
    ) {
        Image(
            bitmap = mandelbrotBitmap.asImageBitmap(),
            contentDescription = "Mandelbrot Set",
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it },
        )

        Text(
            text = "Eval Time: ${evalTime.inWholeMilliseconds} ms",
            modifier = Modifier
                .padding(bottom = 5.dp)
                .align(Alignment.BottomCenter),
        )
    }
}
