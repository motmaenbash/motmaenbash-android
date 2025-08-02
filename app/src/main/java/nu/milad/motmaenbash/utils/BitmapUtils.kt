package nu.milad.motmaenbash.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap

fun Drawable.toSafeBitmap(size: Int = 96): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}