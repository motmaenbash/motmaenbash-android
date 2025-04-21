package nu.milad.motmaenbash.models


data class AppUpdate(
    val latestVersionName: String,
    val forceUpdate: Boolean,
    val links: List<Pair<String, String>>
)
