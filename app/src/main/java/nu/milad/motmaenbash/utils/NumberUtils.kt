package nu.milad.motmaenbash.utils

object NumberUtils {


    fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }
}