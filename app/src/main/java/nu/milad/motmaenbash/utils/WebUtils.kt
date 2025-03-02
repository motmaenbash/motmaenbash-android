package nu.milad.motmaenbash.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import nu.milad.motmaenbash.R


object WebUtils {
    fun openUrlInCustomTab(context: Context, url: String) {

        // Define the color scheme for the custom tab
        val colorSchemeParams = CustomTabColorSchemeParams.Builder().setToolbarColor(
            ContextCompat.getColor(context, R.color.colorPrimary)
        ).build()


        val builder = CustomTabsIntent.Builder()
        builder.setDefaultColorSchemeParams(colorSchemeParams)


        val intent = builder.build()

        try {
            intent.launchUrl(context, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            // No suitable browser is found
            Toast.makeText(context, "مرورگر مناسبی یافت نشد", Toast.LENGTH_SHORT).show()

        }
    }
}