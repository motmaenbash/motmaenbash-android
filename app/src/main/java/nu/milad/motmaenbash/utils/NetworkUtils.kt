package nu.milad.motmaenbash.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat

object NetworkUtils {

    /**
     * Checks if the device is connected to the internet.
     *
     * @param context The context to use for checking connectivity.
     * @return True if the device is connected to the internet, false otherwise.
     */
    fun isInternetAvailable(context: Context): Boolean {

        val connectivityManager =
            ContextCompat.getSystemService(context, ConnectivityManager::class.java) ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)


        } else {
            // Deprecated for API 29 and below, consider alternative approaches.
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}
