package nu.milad.motmaenbash.models

import android.graphics.drawable.Drawable

data class App(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable?,
    val versionCode: Long,
    val apkHash: String,
    val sighHash: String,
    val versionName: String,
    val installSource: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val permissions: List<String>,
)
