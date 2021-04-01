package dk.itu.continuousauthentication.utils

import android.graphics.*
import android.util.Log
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** Utils functions for bitmap conversions.  */
object BitmapUtils {
    private const val TAG = "ByteBufferUtils"

    /** Converts NV21 format byte buffer to bitmap.  */
    fun getBitmap(data: ByteBuffer, metadata: Frame): Bitmap? {
        data.rewind()
        val imageInBuffer = ByteArray(data.limit())
        data[imageInBuffer, 0, imageInBuffer.size]
        try {
            val image = YuvImage(
                imageInBuffer,
                ImageFormat.NV21,
                metadata.size.width,
                metadata.size.height,
                null
            )
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(
                Rect(
                    0,
                    0,
                    metadata.size.width,
                    metadata.size.height
                ), 80, stream
            )
            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()
            return rotateBitmap(
                bmp,
                metadata.rotation,
                true,
                false
            )
        } catch (e: Exception) {
            Log.e("VisionProcessorBase", "Error: " + e.message)
        }
        return null
    }

    /** Rotates a bitmap if it is converted from a bytebuffer.  */
    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }
}