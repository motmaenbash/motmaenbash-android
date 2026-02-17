package nu.milad.motmaenbash.models

enum class ApkStatus {
    READABLE,       // APK can be read and processed
    ENCRYPTED,      // APK is encrypted and cannot be read
    CORRUPTED,      // APK file is corrupted or damaged
    INVALID_FORMAT, // File is not a valid APK/ZIP format
    EMPTY,          // APK exists but contains no entries
    ERROR           // Error occurred during validation
}