package nu.milad.motmaenbash.utils

import android.Manifest

/**
 * Utility class for analyzing app permissions and detecting risky combinations
 */
object PermissionAnalyzer {


    /**
     * Checks if the app has any risky permission combinations
     */
    fun hasRiskyPermissionCombination(permissions: List<String>): Boolean {
        val permissionSet = permissions.toSet()

        return HIGH_RISK_PERMISSION_COMBINATIONS.any { riskyCombo ->
            riskyCombo.all { permission -> permissionSet.contains(permission) }
        }
    }

    /**
     * Gets a list of detected risky permission combinations
     */
    fun getDetectedRiskyPermissionCombinations(permissions: List<String>): List<Set<String>> {
        val permissionSet = permissions.toSet()

        return HIGH_RISK_PERMISSION_COMBINATIONS.filter { riskyCombo ->
            riskyCombo.all { permission -> permissionSet.contains(permission) }
        }
    }

    /**
     * Gets Persian description for risky permission combinations
     */
    fun getRiskyPermissionCombinationDescription(combo: Set<String>): String {
        val persianNames = combo.mapNotNull { permission ->
            PERMISSION_TITLES[permission]
        }

        return if (persianNames.isNotEmpty()) {
            persianNames.joinToString(" + ")
        } else {
            combo.joinToString(" + ")
        }
    }


    private val HIGH_RISK_PERMISSION_COMBINATIONS = listOf(
        // SMS & Contact combinations (Banking trojans)
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS),
        setOf(Manifest.permission.INTERNET, Manifest.permission.READ_SMS),
        setOf(Manifest.permission.INTERNET, Manifest.permission.RECEIVE_SMS),
        // Accessibility service abuse
        setOf(Manifest.permission.INTERNET, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
    )

    private val PERMISSION_TITLES = mapOf(
        Manifest.permission.INTERNET to "دسترسی به اینترنت",
        Manifest.permission.SEND_SMS to "ارسال پیامک",
        Manifest.permission.READ_SMS to "خواندن پیامک‌ها",
        Manifest.permission.RECEIVE_SMS to "دریافت پیامک",
        Manifest.permission.READ_CONTACTS to "دسترسی به مخاطب‌ها",
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to "دسترسی کامل به صفحه"
    )


//    /**
//     * Gets Persian title for a specific permission
//     */
//    fun getPermissionTitle(permission: String): String? {
//        return PERMISSION_TITLES[permission]
//    }
//
//    /**
//     * Gets detailed Persian description for a permission
//     */
//    fun getPermissionDescription(permission: String): String {
//        return PERMISSION_DESCRIPTIONS[permission] ?: "دسترسی نامشخص"
//    }
//
//    /**
//     * Categorizes permissions into different risk levels
//     */
//    fun categorizePermissionsByRisk(permissions: List<String>): Map<RiskLevel, List<String>> {
//        return permissions.groupBy { permission ->
//            when {
//                HIGH_RISK_PERMISSIONS.contains(permission) -> RiskLevel.HIGH
//                MEDIUM_RISK_PERMISSIONS.contains(permission) -> RiskLevel.MEDIUM
//                LOW_RISK_PERMISSIONS.contains(permission) -> RiskLevel.LOW
//                else -> RiskLevel.UNKNOWN
//            }
//        }
//    }
//
//    /**
//     * Gets risk level for a specific permission
//     */
//    fun getPermissionRiskLevel(permission: String): RiskLevel {
//        return when {
//            HIGH_RISK_PERMISSIONS.contains(permission) -> RiskLevel.HIGH
//            MEDIUM_RISK_PERMISSIONS.contains(permission) -> RiskLevel.MEDIUM
//            LOW_RISK_PERMISSIONS.contains(permission) -> RiskLevel.LOW
//            else -> RiskLevel.UNKNOWN
//        }
//    }
//
//    /**
//     * Checks if a permission combination suggests potential banking trojan behavior
//     */
//    fun isBankingTrojanSuspicious(permissions: List<String>): Boolean {
//        val permissionSet = permissions.toSet()
//        return BANKING_TROJAN_PATTERNS.any { pattern ->
//            pattern.all { permission -> permissionSet.contains(permission) }
//        }
//    }
//
//    /**
//     * Checks if a permission combination suggests potential spyware behavior
//     */
//    fun isSpywareSuspicious(permissions: List<String>): Boolean {
//        val permissionSet = permissions.toSet()
//        return SPYWARE_PATTERNS.any { pattern ->
//            pattern.all { permission -> permissionSet.contains(permission) }
//        }
//    }
//
//    /**
//     * Gets a detailed analysis of permissions with explanations
//     */
//    fun getDetailedPermissionAnalysis(permissions: List<String>): PermissionAnalysisResult {
//        val riskyCombs = getDetectedRiskyPermissionCombinations(permissions)
//        val categorized = categorizePermissionsByRisk(permissions)
//        val isBankingTrojan = isBankingTrojanSuspicious(permissions)
//        val isSpyware = isSpywareSuspicious(permissions)
//
//        return PermissionAnalysisResult(
//            totalPermissions = permissions.size,
//            riskyComboCount = riskyCombs.size,
//            highRiskPermissions = categorized[RiskLevel.HIGH] ?: emptyList(),
//            mediumRiskPermissions = categorized[RiskLevel.MEDIUM] ?: emptyList(),
//            detectedRiskyCombs = riskyCombs,
//            isBankingTrojanSuspicious = isBankingTrojan,
//            isSpywareSuspicious = isSpyware,
//            overallRiskLevel = calculateOverallRisk(
//                riskyCombs,
//                categorized,
//                isBankingTrojan,
//                isSpyware
//            )
//        )
//    }
//
//    private fun calculateOverallRisk(
//        riskyCombs: List<Set<String>>,
//        categorized: Map<RiskLevel, List<String>>,
//        isBankingTrojan: Boolean,
//        isSpyware: Boolean
//    ): RiskLevel {
//        return when {
//            isBankingTrojan || isSpyware || riskyCombs.isNotEmpty() -> RiskLevel.HIGH
//            (categorized[RiskLevel.HIGH]?.size ?: 0) > 2 -> RiskLevel.HIGH
//            (categorized[RiskLevel.HIGH]?.size ?: 0) > 0 ||
//                    (categorized[RiskLevel.MEDIUM]?.size ?: 0) > 3 -> RiskLevel.MEDIUM
//
//            else -> RiskLevel.LOW
//        }
//    }
//
//    // Risk level enumeration
//    enum class RiskLevel {
//        LOW, MEDIUM, HIGH, UNKNOWN
//    }
//
//    // Data class for detailed analysis results
//    data class PermissionAnalysisResult(
//        val totalPermissions: Int,
//        val riskyComboCount: Int,
//        val highRiskPermissions: List<String>,
//        val mediumRiskPermissions: List<String>,
//        val detectedRiskyCombs: List<Set<String>>,
//        val isBankingTrojanSuspicious: Boolean,
//        val isSpywareSuspicious: Boolean,
//        val overallRiskLevel: RiskLevel
//    )
//
//    // High-risk permission combinations that are commonly found in malware
//    private val HIGH_RISK_PERMISSION_COMBINATIONS = listOf(
//        // SMS & Contact combinations (Banking trojans)
//        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS),
//        setOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
//        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS),
//        setOf(Manifest.permission.INTERNET, Manifest.permission.READ_SMS),
//        setOf(Manifest.permission.INTERNET, Manifest.permission.RECEIVE_SMS),
//
//        // Accessibility service abuse
//        setOf(Manifest.permission.INTERNET, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
//        setOf(Manifest.permission.SEND_SMS, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
//
//        // Location + Network combinations (Stalkerware)
//        setOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.INTERNET,
//            Manifest.permission.CAMERA
//        ),
//        setOf(
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.INTERNET,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ),
//
//        // Phone state + Network (Premium dialers)
//        setOf(
//            Manifest.permission.CALL_PHONE,
//            Manifest.permission.INTERNET,
//            Manifest.permission.READ_PHONE_STATE
//        ),
//
//        // Device admin + Network (Ransomware)
//        setOf(Manifest.permission.BIND_DEVICE_ADMIN, Manifest.permission.INTERNET),
//
//        // System overlay + Accessibility (Overlay attacks)
//        setOf(
//            Manifest.permission.SYSTEM_ALERT_WINDOW,
//            Manifest.permission.BIND_ACCESSIBILITY_SERVICE
//        )
//    )
//
//    // Specific patterns for banking trojans
//    private val BANKING_TROJAN_PATTERNS = listOf(
//        setOf(
//            Manifest.permission.SEND_SMS,
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.RECEIVE_SMS,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
//            Manifest.permission.SEND_SMS,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.SYSTEM_ALERT_WINDOW,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.INTERNET
//        )
//    )
//
//    // Specific patterns for spyware
//    private val SPYWARE_PATTERNS = listOf(
//        setOf(
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.READ_CALL_LOG,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.INTERNET
//        ),
//        setOf(
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.INTERNET
//        )
//    )
//
//    // High-risk individual permissions
//    private val HIGH_RISK_PERMISSIONS = setOf(
//        Manifest.permission.SEND_SMS,
//        Manifest.permission.CALL_PHONE,
//        Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
//        Manifest.permission.BIND_DEVICE_ADMIN,
//        Manifest.permission.SYSTEM_ALERT_WINDOW,
//        Manifest.permission.REQUEST_INSTALL_PACKAGES,
//        Manifest.permission.WRITE_SECURE_SETTINGS,
//        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
//    )
//
//    // Medium-risk individual permissions
//    private val MEDIUM_RISK_PERMISSIONS = setOf(
//        Manifest.permission.READ_SMS,
//        Manifest.permission.RECEIVE_SMS,
//        Manifest.permission.READ_CONTACTS,
//        Manifest.permission.CAMERA,
//        Manifest.permission.RECORD_AUDIO,
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.READ_CALL_LOG,
//        Manifest.permission.WRITE_CALL_LOG,
//        Manifest.permission.READ_PHONE_STATE,
//        Manifest.permission.PROCESS_OUTGOING_CALLS,
//        Manifest.permission.GET_ACCOUNTS
//    )
//
//    // Low-risk individual permissions
//    private val LOW_RISK_PERMISSIONS = setOf(
//        Manifest.permission.INTERNET,
//        Manifest.permission.ACCESS_NETWORK_STATE,
//        Manifest.permission.ACCESS_WIFI_STATE,
//        Manifest.permission.VIBRATE,
//        Manifest.permission.WAKE_LOCK,
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.RECEIVE_BOOT_COMPLETED
//    )
//
//    // Persian titles for permissions
//    private val PERMISSION_TITLES = mapOf(
//        Manifest.permission.INTERNET to "دسترسی به اینترنت",
//        Manifest.permission.SEND_SMS to "ارسال پیامک",
//        Manifest.permission.READ_SMS to "خواندن پیامک‌ها",
//        Manifest.permission.RECEIVE_SMS to "دریافت پیامک",
//        Manifest.permission.READ_CONTACTS to "دسترسی به مخاطب‌ها",
//        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to "دسترسی کامل به صفحه",
//        Manifest.permission.CAMERA to "دسترسی به دوربین",
//        Manifest.permission.RECORD_AUDIO to "ضبط صدا",
//        Manifest.permission.ACCESS_FINE_LOCATION to "موقعیت مکانی دقیق",
//        Manifest.permission.ACCESS_COARSE_LOCATION to "موقعیت مکانی تقریبی",
//        Manifest.permission.CALL_PHONE to "برقراری تماس",
//        Manifest.permission.READ_CALL_LOG to "خواندن تاریخچه تماس",
//        Manifest.permission.WRITE_CALL_LOG to "تغییر تاریخچه تماس",
//        Manifest.permission.READ_PHONE_STATE to "خواندن وضعیت تلفن",
//        Manifest.permission.SYSTEM_ALERT_WINDOW to "نمایش روی برنامه‌های دیگر",
//        Manifest.permission.BIND_DEVICE_ADMIN to "مدیریت دستگاه",
//        Manifest.permission.REQUEST_INSTALL_PACKAGES to "نصب برنامه",
//        Manifest.permission.WRITE_SECURE_SETTINGS to "تغییر تنظیمات سیستم",
//        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE to "خواندن اعلان‌ها",
//        Manifest.permission.GET_ACCOUNTS to "دسترسی به حساب‌های کاربری",
//        Manifest.permission.PROCESS_OUTGOING_CALLS to "پردازش تماس‌های خروجی",
//        Manifest.permission.ACCESS_NETWORK_STATE to "وضعیت شبکه",
//        Manifest.permission.ACCESS_WIFI_STATE to "وضعیت WiFi",
//        Manifest.permission.VIBRATE to "لرزش دستگاه",
//        Manifest.permission.WAKE_LOCK to "بیدار نگه داشتن صفحه",
//        Manifest.permission.READ_EXTERNAL_STORAGE to "خواندن حافظه خارجی",
//        Manifest.permission.WRITE_EXTERNAL_STORAGE to "نوشتن در حافظه خارجی",
//        Manifest.permission.RECEIVE_BOOT_COMPLETED to "اجرا در شروع سیستم"
//    )
//
//    // Detailed Persian descriptions for permissions
//    private val PERMISSION_DESCRIPTIONS = mapOf(
//        Manifest.permission.SEND_SMS to "امکان ارسال پیامک که ممکن است هزینه‌بر باشد",
//        Manifest.permission.READ_SMS to "خواندن تمام پیامک‌های دریافتی شما",
//        Manifest.permission.RECEIVE_SMS to "دریافت و پردازش پیامک‌های جدید",
//        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to "دسترسی کامل به محتوای صفحه و امکان کنترل برنامه‌ها",
//        Manifest.permission.CAMERA to "استفاده از دوربین برای عکسبرداری و فیلمبرداری",
//        Manifest.permission.RECORD_AUDIO to "ضبط صداهای محیط و مکالمات",
//        Manifest.permission.ACCESS_FINE_LOCATION to "دسترسی به موقعیت دقیق مکانی شما",
//        Manifest.permission.CALL_PHONE to "برقراری تماس که ممکن است هزینه‌بر باشد",
//        Manifest.permission.SYSTEM_ALERT_WINDOW to "نمایش پنجره‌هایی روی سایر برنامه‌ها",
//        Manifest.permission.BIND_DEVICE_ADMIN to "کنترل کامل بر روی دستگاه شما"
//    )
//

}