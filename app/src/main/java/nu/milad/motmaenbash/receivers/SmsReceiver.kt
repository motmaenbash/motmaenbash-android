package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import nu.milad.motmaenbash.ui.AlertDialogActivity
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.SmsUtils
import nu.milad.motmaenbash.utils.SmsUtils.getSmsMessageFromPdu


class SmsReceiver : BroadcastReceiver() {

    private val TAG = "SmsReceiver"
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            val bundle: Bundle? = intent.extras
            val dbHelper = DatabaseHelper(context)

            bundle?.let {
                val pdus = it.get("pdus") as? Array<ByteArray>
                pdus?.forEach { pdu ->
                    val smsMessage = getSmsMessageFromPdu(pdu, bundle)
                    processSmsMessage(context, smsMessage, dbHelper)
                }
            }
    private fun processSmsMessage(
        context: Context, smsMessage: SmsMessage, dbHelper: DatabaseHelper
    ) {
        val sender = smsMessage.displayOriginatingAddress ?: return
        val messageBody = smsMessage.messageBody ?: return


        }

        }
    }


  


}
