package gomeng.dev.stashplayer.core.player

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

class VolumeController(context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun currentFraction(): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return current.toFloat() / max.toFloat()
    }

    fun setFraction(value: Float): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val coerced = value.coerceIn(0f, 1f)
        val volume = (coerced * max).roundToInt().coerceIn(0, max)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        return volume.toFloat() / max.toFloat()
    }

    fun adjustBy(deltaFraction: Float): Float = setFraction(currentFraction() + deltaFraction)

    fun label(value: Float = currentFraction()): String = stashString(R.string.auto_kr_0288, (value.coerceIn(0f, 1f) * 100f).roundToInt())
}
