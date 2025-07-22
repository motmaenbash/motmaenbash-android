package nu.milad.motmaenbash.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri


object WebUtils {
    fun openUrl(context: Context, url: String) {

        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No browser is found
            Toast.makeText(context, "مرورگر یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}