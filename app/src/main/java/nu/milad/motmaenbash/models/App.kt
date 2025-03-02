package nu.milad.motmaenbash.model

import android.graphics.drawable.Drawable

data class App(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable?,
    val versionCode: Long,
    val sha1: String,
    val apkSha1: String,
    val versionName: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val permissions: List<String>,
)
