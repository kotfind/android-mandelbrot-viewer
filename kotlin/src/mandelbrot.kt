package org.kotfind.android_course

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

abstract class MandelbrotGenerator {
    var centerX = 0.0
    var centerY = 0.0
    var range = 3.0
    var bitmapSize = 512
    var maxIter = 100

    abstract fun getType(): String

    protected abstract fun genPixels(): IntArray

    final fun genBitmap(): Bitmap {
        val pixels = genPixels()

        if (pixels.size != bitmapSize * bitmapSize) {
            throw IllegalStateException("Pixel array size is not equal to bitmap size.")
        }

        var bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize)
        return bitmap
    }

    fun toOptionsString(): String {
        return "" +
            "type=${getType()}\n" +
            "centerX=${centerX}\n" +
            "centerY=${centerY}\n" +
            "range=${range}\n" +
            "bitmapSize=${bitmapSize}\n" +
            "maxIter=${maxIter}\n"
    }

    companion object {
        fun fromType(type: String): MandelbrotGenerator {
            return when (type) {
                "kotlin" -> KotlinMandelbrotGenerator()
                "rust" -> RustMandelbrotGenerator()
                else -> throw IllegalArgumentException("Unknown generator type: $type")
            }
        }

        fun clone(oldGen: MandelbrotGenerator): MandelbrotGenerator {
            val newGen = MandelbrotGenerator.fromType(oldGen.getType())
            newGen.centerX = oldGen.centerX
            newGen.centerY = oldGen.centerY
            newGen.range = oldGen.range
            newGen.bitmapSize = oldGen.bitmapSize
            newGen.maxIter = oldGen.maxIter
            return newGen
        }
    }
}

class RustMandelbrotGenerator : MandelbrotGenerator() {
    companion object {
        init {
            // XXX: hardcoded "rust_jni"
            System.loadLibrary("rust_jni")
        }
    }

    override fun getType(): String {
        return "rust"
    }

    override external fun genPixels(): IntArray
}

class KotlinMandelbrotGenerator : MandelbrotGenerator() {
    override fun getType(): String {
        return "kotlin"
    }

    override fun genPixels(): IntArray {
        val pixels = IntArray(bitmapSize * bitmapSize)

        for (y in 0..<bitmapSize) {
            for (x in 0..<bitmapSize) {
                val idx = y * bitmapSize + x

                val mathX = x.toDouble() / bitmapSize * range - range / 2.0 + centerX
                val mathY = y.toDouble() / bitmapSize * range - range / 2.0 + centerY
                val c = Complex(mathX, mathY)

                val hue = (getPointIters(c) * 360.0 / 100.0) % 360.0
                pixels[idx] = Color.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f))
            }
        }

        return pixels
    }

    private fun getPointIters(c: Complex): Int {
        var z = Complex(0.0, 0.0)
        for (iter in 0..maxIter) {
            if (z.len() > 2) {
                return iter
            }

            z = z * z + c
        }
        return maxIter
    }
}

data class Complex(
    var a: Double,
    var b: Double,
) {
    operator fun plus(other: Complex): Complex {
        return Complex(a + other.a, b + other.b)
    }

    operator fun times(other: Complex): Complex {
        return Complex(a * other.a - b * other.b, a * other.b + b * other.a)
    }

    operator fun times(other: Double): Complex {
        return Complex(a * other, b * other)
    }

    operator fun minus(other: Complex): Complex {
        return Complex(a - other.a, b - other.b)
    }

    operator fun plusAssign(other: Complex) {
        val res = this * other
        this.a = res.a
        this.b = res.b
    }

    operator fun minusAssign(other: Complex) {
        val res = this - other
        this.a = res.a
        this.b = res.b
    }

    operator fun timesAssign(other: Double) {
        val res = this * other
        this.a = res.a
        this.b = res.b
    }

    operator fun timesAssign(other: Complex) {
        val res = this * other
        this.a = res.a
        this.b = res.b
    }

    fun len(): Double {
        return sqrt(a * a + b * b)
    }

    fun dist(other: Complex): Double {
        return (this - other).len()
    }
}
