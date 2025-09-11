package nu.milad.motmaenbash.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alert(
    val type: AlertType,
    val threatType: UrlThreatType? = null,
    val level: AlertLevel,
    val title: String,
    val summary: String? = null,
    val content: String? = null,
    val param1: String,
    val param2: String?,
    val param3: String? = null
) : Parcelable {

    enum class AlertLevel(val value: Int) {
        ALERT(1),
        WARNING(2),
        NEUTRAL(3),
        INFO(4);

        companion object {
            fun fromInt(value: Int): AlertLevel? {
                return entries.find { it.value == value }
            }
        }
    }

    enum class AlertType(val value: Int) {
        SMS_SENDER_FLAGGED(1),
        SMS_LINK_FLAGGED(2),
        SMS_KEYWORD_FLAGGED(3),
        SMS_PATTERN_FLAGGED(4),
        SMS_NEUTRAL(5),
        APP_FLAGGED(6),
        URL_FLAGGED(7),
        APP_RISKY_INSTALL(8);


        companion object {
            fun fromInt(value: Int): AlertType? {
                return entries.find { it.value == value }
            }
        }
    }

    enum class UrlThreatType(val value: Int) {
        PHISHING(1),
        SCAM(2),
        PONZI(3),
        Other(4);

        companion object {
            fun fromInt(value: Int): UrlThreatType? {
                return entries.find { it.value == value }
            }
        }
    }

    companion object {
        // SMS alert types list for checking
        val SMS_ALERT_TYPES = listOf(
            AlertType.SMS_SENDER_FLAGGED,
            AlertType.SMS_LINK_FLAGGED,
            AlertType.SMS_KEYWORD_FLAGGED,
            AlertType.SMS_PATTERN_FLAGGED,
            AlertType.SMS_NEUTRAL
        )
    }


}