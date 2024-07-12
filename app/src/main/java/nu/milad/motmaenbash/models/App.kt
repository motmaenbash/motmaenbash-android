package nu.milad.motmaenbash.model

data class App(
    val appName: String,
    val packageName: String,
    val versionCode: Long,
    val sha1: String,
    val apkSha1: String,
)
