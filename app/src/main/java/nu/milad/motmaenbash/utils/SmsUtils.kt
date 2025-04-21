package nu.milad.motmaenbash.utils

import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import nu.milad.motmaenbash.utils.TextUtils.arabicToPersian
import nu.milad.motmaenbash.utils.TextUtils.removeShortWords

object SmsUtils {


    /**
     * Retrieves an SmsMessage object from a PDU (Protocol Data Unit).
     *
     * @param pdu The PDU byte array.
     * @param bundle The bundle containing the SMS data (optional for API levels below M).
     * @return The SmsMessage object.
     */
    fun getSmsMessageFromPdu(pdu: ByteArray, bundle: Bundle? = null): SmsMessage {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val format = bundle?.getString("format") ?: "3gpp"
            SmsMessage.createFromPdu(pdu, format)
        } else {
            SmsMessage.createFromPdu(pdu)
        }
    }


    /**
     * Normalizes and hashes a message for comparisonand storage.
     *
     * @param message The raw SMS message string.
     * @return The SHA-256 hash of the normalized message, or null if the normalized message is empty.
     */
    fun generateNormalizedMessageHash(message: String): String? {
        val normalizedMessage = normalizeMessage(message)

        Log.d("SmsUtils", "Final normalized: $normalizedMessage")

        return if (normalizedMessage.isNotBlank()) {
            val hash = HashUtils.generateSHA256(
                normalizedMessage.replace(
                    " ", ""
                )
            ) //todo: check if replace or not
            Log.d("SmsUtils", "Generated hash: $hash")
            hash
        } else {
            null
        }
    }

    /**
     * Normalizes a message by performing a series of cleaning and transformation steps.
     *
     * @param message The raw SMS message string.
     * @return The normalized message string.
     */
    private fun normalizeMessage(message: String): String {
        var cleanedMessage = message


        // Step 1: Remove charactersoutside the allowed set (Arabic, Persian, whitespace, zero-width) and Kashida

        cleanedMessage = cleanedMessage.replace(

            Regex(
                "[^\\u0622-\\u064A" + //Arabic characters ا to ي (excluding Hamza)
                        "\\u067E\\u0698\\u06A9\\u06AF\\u06CC\\u0647\\u0648\\u062F\\u0632\\u0686" + // Additional Persian characters (پ ژ ک گ ی ه و د ز چ)
                        "\\s" + //Whitespace characters (space, tab, newline)
                        "\\u200B\\u200C]" +//Zero-width characters (ZWSP, ZWNJ)
                        "|\\u0640"  //Add Kashida (ـ) to be removed

            ), ""
        )

        // Step 2: Replace Arabic characters with Persian equivalents
        cleanedMessage = arabicToPersian(cleanedMessage)

        // Step 3: Normalize whitespace
        cleanedMessage = cleanedMessage.replace(Regex("[\\s\u200B\u200C]+"), " ")

        // Step 4: Remove short words (length < 2) and trim again
        cleanedMessage = removeShortWords(cleanedMessage, 2).trim()


        return cleanedMessage
    }


}