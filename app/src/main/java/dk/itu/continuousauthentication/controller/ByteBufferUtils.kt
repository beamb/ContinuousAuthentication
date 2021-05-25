package dk.itu.continuousauthentication.controller

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ByteBufferUtils {
    fun getImageData(bitmap: Bitmap): ByteBuffer {
        val input = ByteBuffer.allocateDirect(112 * 112 * 3 * 4).order(ByteOrder.nativeOrder())
        for (y in 0 until 112) {
            for (x in 0 until 112) {
                val px = bitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }

        return input
    }

    fun getModelOutput(): ByteBuffer {
        val bufferSize = 128 * java.lang.Float.SIZE / java.lang.Byte.SIZE

        return ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
    }
}