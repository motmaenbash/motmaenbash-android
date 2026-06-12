package nu.milad.motmaenbash.utils

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings

object UsageStatsHelper {

    private const val WINDOW_MS = 7L * 24 * 60 * 60 * 1000

    data class UsageInfo(
        val lastTimeUsed: Long,
        val foregroundMs: Long,
        val foregroundServiceMs: Long,
        val foregroundLaunchCount: Int
    )

    fun hasUsageAccess(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val uid = Process.myUid()
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, uid, context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, uid, context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun usageAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun getUsage(
        context: Context,
        packageName: String,
        now: Long = System.currentTimeMillis()
    ): UsageInfo? {
        if (!hasUsageAccess(context)) return null
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val begin = now - WINDOW_MS

            val stats = usm.queryAndAggregateUsageStats(begin, now)[packageName]
            val lastUsed = stats?.lastTimeUsed ?: 0L
            val foreground = stats?.totalTimeInForeground ?: 0L
            val foregroundService = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                stats?.totalTimeForegroundServiceUsed ?: 0L
            } else 0L

            var launches = 0
            val events = usm.queryEvents(begin, now)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                @Suppress("DEPRECATION")
                if (event.packageName == packageName &&
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                ) {
                    launches++
                }
            }
            UsageInfo(lastUsed, foreground, foregroundService, launches)
        } catch (e: Exception) {
            null
        }
    }

    fun getForegroundLaunchCounts(
        context: Context,
        now: Long = System.currentTimeMillis()
    ): Map<String, Int> {
        if (!hasUsageAccess(context)) return emptyMap()
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usm.queryEvents(now - WINDOW_MS, now)
            val counts = HashMap<String, Int>()
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                @Suppress("DEPRECATION")
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    counts[event.packageName] = (counts[event.packageName] ?: 0) + 1
                }
            }
            counts
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun describe(usage: UsageInfo): String {
        val parts = mutableListOf<String>()
        if (usage.lastTimeUsed > 0) {
            parts.add("آخرین اجرا ${DateUtils.timeAgo(usage.lastTimeUsed)}")
        }
        if (usage.foregroundLaunchCount > 0) {
            parts.add("${NumberUtils.toPersianNumbers(usage.foregroundLaunchCount.toString())} بار در هفته اخیر")
        }
        return parts.joinToString("، ")
    }
}
