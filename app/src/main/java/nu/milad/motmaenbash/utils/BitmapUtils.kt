package nu.milad.motmaenbash.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap

fun Drawable.toSafeBitmap(size: Int = 96): Bitmap {
    val safeSize = size.coerceAtLeast(1)
    val bitmap = createBitmap(safeSize, safeSize)
    val canvas = Canvas(bitmap)
    try {
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
    } catch (e: Exception) {
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
    }
    return bitmap
}
