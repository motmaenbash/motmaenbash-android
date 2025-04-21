package nu.milad.motmaenbash.models


data class Stats(
    val suspiciousLinksDetected: Int = 0,
    val suspiciousSmsDetected: Int = 0,
    val suspiciousAppDetected: Int = 0,
    val verifiedGatewayDetected: Int = 0
)
