package nu.milad.motmaenbash.consts

object AppConstants {


    // Database Constants
    const val DATABASE_NAME = "motmaenbash.db"
    const val DATABASE_VERSION = 1

    // Table names
    const val TABLE_FLAGGED_LINKS = "flagged_links"
    const val TABLE_FLAGGED_SENDERS = "flagged_senders"
    const val TABLE_FLAGGED_MESSAGES = "flagged_messages"
    const val TABLE_FLAGGED_WORDS = "flagged_words"
    const val TABLE_FLAGGED_APPS = "flagged_apps"
    const val TABLE_TIPS = "tips"
    const val TABLE_STATS = "stats"

    // Column names
    const val COLUMN_HASH = "hash"

    // Stats keys
    const val STAT_FLAGGED_LINK_DETECTED = "flagged_link_detected"
    const val STAT_FLAGGED_SMS_DETECTED = "flagged_sms_detected"
    const val STAT_FLAGGED_APP_DETECTED = "flagged_app_detected"


    const val APP_PREFERENCES = "AppPreferences"
    const val PREF_KEY_LAST_UPDATE_TIME = "last_database_update_time"
    const val PREF_KEY_FIRST_LAUNCH = "first_launch"
    const val PREF_KEY_INTRO_SHOWN = "intro_shown"


    const val DONATE_URL = "https://milad.nu/page/donate"


    const val USER_REPORT_FORM_URL =
        "https://docs.google.com/forms/d/e/1FAIpQLSfzb1ueey6qQZdQb9tRm_Z7Mh3o8k_ZYysOv6AqTiQx39ahNg/viewform"


    // URL Endpoints
    const val BASE_URL = "https://github.com/miladnouri/motmaenbash-data/raw/main/data/"
    const val UPDATE_DATA_URL = BASE_URL + "data.json"
    const val UPDATE_TIPS_URL = BASE_URL + "tips.json"
    const val UPDATE_SPONSOR_URL = BASE_URL + "sponsor.json"
    const val UPDATE_APP_URL = BASE_URL + "version.json"


}