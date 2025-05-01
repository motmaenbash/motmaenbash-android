package nu.milad.motmaenbash.utils

import android.app.Activity.VIBRATOR_MANAGER_SERVICE
import android.app.Activity.VIBRATOR_SERVICE
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.viewmodels.SettingsViewModel

/**
 * Handles sound playback for app alerts
 */
class AudioHelper(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>() // Cache for loaded sounds


    // Settings
    private var playSoundInSilentMode = true
    private var playNeutralSmsSound = false
    private var alertSoundName = "ding1"
    private var neutralSmsSoundName = "ding2"


    // Coroutine scope for settings loading
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Constant to map sound names to resource IDs
    private val soundResources = mapOf(
        "ding1" to R.raw.ding1,
        "ding2" to R.raw.ding2,
        "ding3" to R.raw.ding3,
        "ding4" to R.raw.ding4,
        "ding5" to R.raw.ding5,
        "ding6" to R.raw.ding6
    )

    companion object {
        private const val MAX_STREAMS = 2
        private const val SOUND_PRIORITY = 1 // Standard priority
        private const val TAG = "AudioHelper"
    }


    init {
        initializeSoundPool()
        preloadSounds()
    }

    private suspend fun loadSettings() {
        try {
            val preferences = context.dataStore.data.first()

            alertSoundName = preferences[SettingsViewModel.ALERT_SOUND] ?: alertSoundName
            neutralSmsSoundName =
                preferences[SettingsViewModel.NEUTRAL_SMS_SOUND] ?: neutralSmsSoundName
            playSoundInSilentMode = preferences[SettingsViewModel.PLAY_SOUND_IN_SILENT_MODE] ?: true
            playNeutralSmsSound = preferences[SettingsViewModel.PLAY_NEUTRAL_SMS_SOUND] ?: true

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound settings", e)
        }


    }


    /**
     * Initializes the SoundPool with appropriate audio attributes.
     */
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM) // Higher priority than NOTIFICATION
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED) // Enforce playing even in silent mode
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    /**
     * Preloads all defined sounds into the SoundPool.
     */
    private fun preloadSounds() {
        soundResources.forEach { (soundName, resourceId) ->
            val soundId = soundPool?.load(context, resourceId, SOUND_PRIORITY) ?: 0
            soundMap[soundName] = soundId
            if (soundId == 0) {
                Log.e(TAG, "Failed to load sound: $soundName")
            } else {
                Log.d(TAG, "Sound queued for loading: $soundName")
            }
        }

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            val soundName = soundMap.entries.firstOrNull { it.value == sampleId }?.key ?: "unknown"
            if (status == 0) {
                Log.d(TAG, "Sound loaded successfully: $soundName")
            } else {
                Log.e(TAG, "Failed to load sound: $soundName, status: $status")
            }
        }
    }


    suspend fun playAlertSound() {
        loadSettings()
        playSound(alertSoundName, playSoundInSilentMode)

    }


    fun playSound(soundName: String, forceSilentMode: Boolean) {
        val soundId = soundMap[soundName] ?: 0
        if (soundId == 0) {
            Log.e(TAG, "Sound not found: $soundName")
            return
        }

        // Check if we should play in silent mode
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode


        val shouldPlaySound = when {
            // In normal mode, always play
            ringerMode == AudioManager.RINGER_MODE_NORMAL -> true

            // In silent/vibrate mode, play only if forceSilentMode is true (meaning play anyway)
            forceSilentMode -> true

            // Otherwise don't play
            else -> false
        }


        if (!shouldPlaySound) {
            Log.d(TAG, "Skip playing sound: device in silent mode")
            return
        }

        // Play with high priority
        val streamId = soundPool?.play(soundId, 1.0f, 1.0f, 10, 0, 1.0f) ?: 0
        Log.d(TAG, "Playing sound: $soundName, streamId: $streamId")

        if (streamId == 0) {
            // If playback failed, try reloading
            val resourceId = getSoundResourceId(soundName)
            if (resourceId != 0) {
                Log.d(TAG, "Reloading and playing sound: $soundName")
                val newSoundId = soundPool?.load(context, resourceId, 1) ?: 0
                soundMap[soundName] = newSoundId

                // Play after successful loading
                soundPool?.setOnLoadCompleteListener { pool, sampleId, status ->
                    if (status == 0 && sampleId == newSoundId) {
                        pool.play(sampleId, 1.0f, 1.0f, 10, 0, 1.0f)
                        Log.d(TAG, "Playing after reload: $soundName")
                    }
                }
            }
        }
    }

    private fun getSoundResourceId(soundName: String): Int {
        return soundResources[soundName] ?: 0
    }


    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }

    fun vibrateDevice(context: Context) {
        // Initialize Vibrator
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager: VibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") (vibrator.vibrate(500))
        }
    }

    /**
     * Sound types for the application
     */
    enum class SoundType {
        ALERT,
        NEUTRAL_SMS
    }

}