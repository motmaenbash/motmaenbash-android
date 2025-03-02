package nu.milad.motmaenbash.utils

import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage

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
