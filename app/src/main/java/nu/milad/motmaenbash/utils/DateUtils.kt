package nu.milad.motmaenbash.utils


import nu.milad.motmaenbash.utils.NumberUtils.toPersianNumbers
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
     * Get the current time in milliseconds since epoch.
     *
     * @return Current time in milliseconds.
     */
    fun getCurrentTimeInMillis(): Long {
        return System.currentTimeMillis()
    }


    /**
     * Get a human-readable string representing the time elapsed from the given timestamp to now, in Persian.
     *
     * @param timeInMillis The timestamp to calculate the time elapsed from, in milliseconds.
     * @return A string representing the time elapsed in Persian.
     */
    fun timeAgo(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeInMillis

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "لحظاتی قبل"
            diff < TimeUnit.HOURS.toMillis(1) -> "${
                toPersianNumbers(
                    (diff / TimeUnit.MINUTES.toMillis(
                        1
                    )).toString()
                )
            } دقیقه قبل"

            diff < TimeUnit.DAYS.toMillis(1) -> "${
                toPersianNumbers(
                    (diff / TimeUnit.HOURS.toMillis(
                        1
                    )).toString()
                )
            } ساعت قبل"

            diff < TimeUnit.DAYS.toMillis(7) -> "${toPersianNumbers((diff / TimeUnit.DAYS.toMillis(1)).toString())} روز قبل"
            diff < TimeUnit.DAYS.toMillis(30) -> "${
                toPersianNumbers(
                    (diff / TimeUnit.DAYS.toMillis(
                        1
                    ) / 7).toString()
                )
            } هفته قبل"

            diff < TimeUnit.DAYS.toMillis(365) -> "${
                toPersianNumbers(
                    (diff / TimeUnit.DAYS.toMillis(
                        30
                    )).toString()
                )
            } ماه قبل"

            else -> "${toPersianNumbers((diff / TimeUnit.DAYS.toMillis(365)).toString())} سال قبل"
        }
    }


    /**
     * Convert a Gregorian date to Persian (Shamsi) date format.
     *
     * @param gregorianDate The Gregorian date to convert
     * @return Persian date and time as formatted string in "yyyy/MM/dd HH:mm" format
     */
    fun convertGregorianToPersian(gregorianDate: String?): String {

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).parse((gregorianDate.toString()))

        val calendar = PersianCalendar()

        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        calendar.gregorianToPersian(
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
