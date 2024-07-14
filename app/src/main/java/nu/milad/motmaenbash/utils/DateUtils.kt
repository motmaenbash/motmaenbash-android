package nu.milad.motmaenbash.utils


import nu.milad.motmaenbash.utils.NumberUtils.toPersianNumbers
import tech.tookan.emrooz.utils.PersianCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {


    /**
     * Get current date and time in Gregorian calendar format.
     *
     * @return Current date and time as formatted string in "yyyy-MM-dd HH:mm" format
     */
    fun getCurrentTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }




    /**
     * Convert a Gregorian date to Persian (Shamsi) date format.
     *
     * @param gregorianDate The Gregorian date to convert
     * @return Persian date and time as formatted string in "yyyy/MM/dd HH:mm" format
     */
    fun convertGregorianToPersian(gregorianDate: String?): String {

        val gregorianDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse((gregorianDate))

        val calendar = PersianCalendar()

        val cal = Calendar.getInstance()
        cal.time = gregorianDate
        calendar.GregorianToPersian(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,  // Calendar.MONTH is zero-based
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        return String.format(
            Locale.US,
            "%04d/%02d/%02d %02d:%02d",
            calendar.year,
            calendar.month,
            calendar.day,
            hour,
            minute
        )


    }

}