package nu.milad.motmaenbash.utils

import java.util.Locale

object NumberUtils {


    fun formatNumber(number: Int): String {
        return toPersianNumbers(String.format(Locale.getDefault(), "%,d", number))
    }


    fun toPersianNumbers(number: String): String {

        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return number.map {
            when (it) {
                in '0'..'9' -> persianDigits[it - '0']
                else -> it
            }
        }.joinToString("")
    }


}