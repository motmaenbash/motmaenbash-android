package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.AlertLevel
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.SmsUtils.getSmsMessageFromPdu
import nu.milad.motmaenbash.utils.TextUtils.extractLinks
import nu.milad.motmaenbash.utils.TextUtils.removeShortWords


class SmsReceiver : BroadcastReceiver() {

    private val TAG = "SmsReceiver"


    private lateinit var dbHelper: DatabaseHelper

    // HashSet to store unique message IDs
    companion object {
        private val receivedMessages = HashSet<String>()
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        Log.d(TAG, "SMS received")

        val bundle = intent.extras ?: return

        val pdus = bundle.get("pdus") as? Array<ByteArray> ?: return

        val fullMessageBody = StringBuilder()
        var sender: String? = null

        dbHelper = DatabaseHelper(context.applicationContext)




        try {
            pdus.forEach { pdu ->


                val smsMessage = getSmsMessageFromPdu(pdu, bundle)
                sender = sender ?: smsMessage.displayOriginatingAddress
                smsMessage.messageBody?.let(fullMessageBody::append)
            }

            if (fullMessageBody.isNotBlank()) {

                // Generate a unique message ID
                val messageId = "${sender}-${fullMessageBody.toString().hashCode()}"

                // Check if this message ID is already processed
                if (receivedMessages.contains(messageId)) {
                    Log.d(TAG, "Duplicate SMS received, ignoring.")
                    return
                } else {
                    receivedMessages.add(messageId)
                }

                analyzeSmsMessage(context, sender, fullMessageBody.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error processing SMS", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }


    }

    private fun analyzeSmsMessage(
        context: Context, sender: String?, messageBody: String
    ) {


        // Convert to lowercase and trim
        val messageText = removeShortWords(messageBody.lowercase().trim())


        if (messageText.isBlank()) {
            Log.d(TAG, "Received empty SMS message.")
            return
        }

        // Extract links from the message
        val extractedLinks = extractLinks(messageText)

        Log.d(TAG, extractedLinks.toString())


        when {


            // Check if the sender is flagged
            sender != null && dbHelper.isSenderFlagged(sender) -> {
                AlertUtils.showAlert(
                    context,
                    AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED,
                    AlertLevel.ERROR.toString(),
                    sender,
                    messageText,
                )
                return
            }


            // Check if extracted links are flagged
            extractedLinks.isNotEmpty() && dbHelper.hasFlaggedLink(extractedLinks) -> {
                AlertUtils.showAlert(
                    context,
                    AlertHandlerActivity.ALERT_TYPE_SMS_LINK_FLAGGED,
                    AlertLevel.ERROR.toString(),
                    sender,
                    messageText,


                    )
                return
            }


            // Check if the message contains any flagged keywords
            dbHelper.hasFlaggedWord(messageText) -> {
                AlertUtils.showAlert(
                    context,
                    AlertHandlerActivity.ALERT_TYPE_SMS_KEYWORD_FLAGGED,
                    AlertLevel.ERROR.toString(),
                    sender,
                    messageText,

                    )
                return
            }

            // Check if the message pattern is flagged
            dbHelper.isMessageFlagged(messageText) -> {

                AlertUtils.showAlert(
                    context,
                    AlertHandlerActivity.ALERT_TYPE_SMS_PATTERN_FLAGGED,
                    AlertLevel.ERROR.toString(),
                    sender,
                    messageText,

                    )
                return
            }


        }

    }

}
