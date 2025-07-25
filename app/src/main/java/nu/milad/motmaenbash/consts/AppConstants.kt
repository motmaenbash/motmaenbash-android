package nu.milad.motmaenbash.consts

object AppConstants {


    // Database Constants
    const val DATABASE_NAME = "motmaenbash.db"
    const val DATABASE_VERSION = 3

    // Table names
    const val TABLE_FLAGGED_URLS = "flagged_urls"
    const val TABLE_FLAGGED_SENDERS = "flagged_senders"
    const val TABLE_FLAGGED_MESSAGES = "flagged_messages"
    const val TABLE_FLAGGED_WORDS = "flagged_words"
    const val TABLE_FLAGGED_APPS = "flagged_apps"
    const val TABLE_TIPS = "tips"
    const val TABLE_STATS = "stats"
    const val TABLE_UPDATE_HISTORY = "update_history"
    const val TABLE_ALERT_HISTORY = "alert_history"

    // Column names
    const val COLUMN_HASH = "hash"

    // Stats keys
    const val MOTMAENBASH_EVENT = "motmaenbash_alert"
    const val STAT_VERIFIED_GATEWAY = "verified_gateway"
    const val STAT_FLAGGED_LINK_DETECTED = "flagged_link_detected"
    const val STAT_FLAGGED_SMS_DETECTED = "flagged_sms_detected"
    const val STAT_FLAGGED_APP_DETECTED = "flagged_app_detected"
    const val STAT_RISKY_APP_DETECTED = "risky_app_detected"

    const val STAT_TOTAL_SCANNED_LINK = "total_scanned_link"
    const val STAT_TOTAL_SCANNED_SMS = "total_scanned_sms"
    const val STAT_TOTAL_SCANNED_APP = "total_scanned_app"

    const val APP_PREFERENCES = "AppPreferences"
    const val PREF_KEY_LAST_UPDATE_TIME = "last_database_update_time"

    // const val PREF_KEY_FIRST_LAUNCH = "first_launch"
    const val PREF_KEY_HAS_SEEN_INTRO = "has_seen_intro"
    const val PREF_KEY_LAST_CHANGELOG_SHOW_VERSION = "last_changelog_show_version"

    const val DONATE_URL = "https://motmaenbash.ir/donate.html"


    const val USER_REPORT_FORM_URL =
        "https://docs.google.com/forms/d/e/1FAIpQLSfzb1ueey6qQZdQb9tRm_Z7Mh3o8k_ZYysOv6AqTiQx39ahNg/viewform"

    // URL Endpoints
    const val BASE_URL = "https://github.com/miladnouri/motmaenbash-data/raw/main/data/"
    const val UPDATE_DATA_URL = BASE_URL + "data.json"
    const val UPDATE_TIPS_URL = BASE_URL + "tips.json"
    const val UPDATE_LINKS_URL = BASE_URL + "links.json"
    const val UPDATE_APP_URL = BASE_URL + "version.json"


}