package nu.milad.motmaenbash.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import nu.milad.motmaenbash.R


object WebUtils {
    fun openUrlInCustomTab(context: Context, url: String) {

        // Define the color scheme for the custom tab
        val colorSchemeParams = CustomTabColorSchemeParams.Builder().setToolbarColor(
            ContextCompat.getColor(context, R.color.colorPrimary)
        ).build()

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .build()

        try {
            intent.launchUrl(context, url.toUri())
        } catch (e: ActivityNotFoundException) {
            // No suitable browser is found
            Toast.makeText(context, "مرورگر مناسبی یافت نشد", Toast.LENGTH_SHORT).show()

        }
    }

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