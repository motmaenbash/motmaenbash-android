package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.SmsUtils.getSmsMessageFromPdu
import nu.milad.motmaenbash.utils.TextUtils.extractLinks
import nu.milad.motmaenbash.utils.TextUtils.removeShortWords


class SmsReceiver : BroadcastReceiver() {


    companion object {
        private const val TAG = "SmsReceiver"

        // HashSet to store unique message IDs
        private val receivedMessages = HashSet<String>()
    }

    private lateinit var dbHelper: DatabaseHelper


    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return


        val bundle = intent.extras ?: return

        val pdus = bundle.get("pdus") as? Array<ByteArray> ?: return

        val fullMessageBody = StringBuilder()
        var sender: String? = null
        var timestamp: Long? = null

        dbHelper = DatabaseHelper(context.applicationContext)




        try {
            pdus.forEachIndexed { index, pdu ->


                val smsMessage = getSmsMessageFromPdu(pdu, bundle)

                smsMessage.messageBody?.let(fullMessageBody::append)
                // Set sender and timestamp only once from the first SMS part
                if (index == 0) {
                    sender = smsMessage.displayOriginatingAddress
                    timestamp = smsMessage.timestampMillis
                }
            }

            if (fullMessageBody.isNotBlank()) {

                // Generate a unique message ID
                val messageId = "${sender}-${fullMessageBody.toString().hashCode()}-${timestamp}"

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


        // Determine alert type
        val alertType = when {


            // Check if extracted links are flagged
            extractedLinks.isNotEmpty() && dbHelper.hasFlaggedLink(extractedLinks) ->
                Alert.AlertType.SMS_LINK_FLAGGED

            // Check if the message contains any flagged keywords
            dbHelper.hasFlaggedWord(messageText) ->
                Alert.AlertType.SMS_KEYWORD_FLAGGED

            // Check if the message pattern is flagged
            dbHelper.isMessageFlagged(messageText) ->
                Alert.AlertType.SMS_PATTERN_FLAGGED

            // Check if the sender is flagged
            sender != null && dbHelper.isSenderFlagged(sender) ->
                Alert.AlertType.SMS_SENDER_FLAGGED

            // No flagged content detected
            else -> Alert.AlertType.SMS_NEUTRAL


        }

        // Show alert if needed
        alertType.let {
            AlertUtils.showAlert(
                context = context,
                alertType = it,
                alertLevel = if (it == Alert.AlertType.SMS_SENDER_FLAGGED) Alert.AlertLevel.WARNING else if (it == Alert.AlertType.SMS_NEUTRAL) Alert.AlertLevel.NEUTRAL else Alert.AlertLevel.ALERT,
                param1 = sender!!,
                param2 = messageText
            )

        }

    }

}
