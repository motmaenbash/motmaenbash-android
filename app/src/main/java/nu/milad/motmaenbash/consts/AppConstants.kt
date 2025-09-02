package nu.milad.motmaenbash.consts

object AppConstants {


    // Database Constants
    const val DATABASE_NAME = "motmaenbash.db"
    const val DATABASE_VERSION = 5

    // Table names
    const val TABLE_FLAGGED_URLS = "flagged_urls"
    const val TABLE_FLAGGED_SMS = "flagged_sms"
    const val TABLE_FLAGGED_APPS = "flagged_apps"
    const val TABLE_TRUSTED_ENTITIES = "trusted_entities"
    const val TABLE_TIPS = "tips"
    const val TABLE_STATS = "stats"
    const val TABLE_UPDATE_HISTORY = "update_history"
    const val TABLE_ALERT_HISTORY = "alert_history"

    // Only used for drop old tables
    const val TABLE_FLAGGED_SENDERS = "flagged_senders"
    const val TABLE_FLAGGED_MESSAGES = "flagged_messages"
    const val TABLE_FLAGGED_WORDS = "flagged_words"

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

    // Flagged App Types
    object MaliciousAppType {
        const val PACKAGE = 1
        const val APK = 2
        const val SIGN = 3
        const val DEX = 4
    }

    // Trusted Entity Types
    object TrustedEntityType {
        const val MARKET_PACKAGE = 1
        const val SIDELOAD_COMBINED = 2
    }

    // SMS Threat Types
    object SmsThreatType {
        const val SENDER = 1
        const val PATTERN = 2
        const val KEYWORD = 3
    }

    // URL Match Types
    object UrlMatchType {
        const val DOMAIN = 1
        const val SPECIFIC_URL = 2
    }

    // Map indices to table names
    object DataMapping {
        val INDEX_TO_TABLE_MAP = mapOf(
            0 to TABLE_FLAGGED_URLS,
            1 to TABLE_FLAGGED_SMS,
            2 to TABLE_FLAGGED_APPS
        )
    }

    const val APP_PREFERENCES = "AppPreferences"
    const val PREF_KEY_LAST_UPDATE_TIME = "last_database_update_time"

    // const val PREF_KEY_FIRST_LAUNCH = "first_launch"
    const val PREF_KEY_HAS_SEEN_INTRO = "has_seen_onboarding"
    const val PREF_KEY_LAST_CHANGELOG_SHOW_VERSION = "last_changelog_show_version"

    const val DONATE_URL = "https://motmaenbash.ir/donate.html"


    const val USER_REPORT_FORM_URL =
        "https://docs.google.com/forms/d/e/1FAIpQLSfzb1ueey6qQZdQb9tRm_Z7Mh3o8k_ZYysOv6AqTiQx39ahNg/viewform"

    // URL Endpoints
    const val BASE_URL = "https://github.com/miladnouri/motmaenbash-data/raw/main/data/"
    const val UPDATE_DATA_URL = BASE_URL + "malicious-data.json"
    const val UPDATE_TRUSTED_ENTITIES_URL = BASE_URL + "trusted-data.json"
    const val UPDATE_TIPS_URL = BASE_URL + "tips.json"
    const val UPDATE_LINKS_URL = BASE_URL + "links.json"
    const val UPDATE_APP_URL = BASE_URL + "version.json"


}