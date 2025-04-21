package nu.milad.motmaenbash.models

data class AlertHistory(
    val id: Long = 0,
    val type: AlertHistoryType,
    val timestamp: Long = System.currentTimeMillis(),
    val param1: String? = null,
    val param2: String? = null
) {
    enum class AlertHistoryType(val value: Int) {
        SMS(1), LINK(2), APP(3), UNKNOWN(0);

        companion object {
            fun fromValue(value: Int): AlertHistoryType {
                return entries.find { it.value == value } ?: UNKNOWN
            }
        }
    }
}