package nu.milad.motmaenbash.models

data class UpdateHistory(
    val type: UpdateType,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class UpdateType(val value: Int) {
        MANUAL(1), AUTO(2)
    }
}