package nu.milad.motmaenbash.utils

import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage

object SmsUtils {

    /**
     * Retrieves an SmsMessage object from a PDU (Protocol Data Unit).
     *
     * @param pdu The PDU byte array.
     * @param bundle The bundle containing the SMS data.
     * @return The SmsMessage object.
     */
    fun getSmsMessageFromPdu(pdu: Any, bundle: Bundle): SmsMessage {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val format = bundle.getString("format")
            SmsMessage.createFromPdu(pdu as ByteArray, format)
        } else {
            SmsMessage.createFromPdu(pdu as ByteArray)
        }
    }
}
