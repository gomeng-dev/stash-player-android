package gomeng.dev.stashplayer.core.player

import android.app.Activity
import kotlin.math.roundToInt
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

class BrightnessController(private val activity: Activity) {
    fun currentFraction(): Float {
        val value = activity.window.attributes.screenBrightness
        return if (value >= 0f) value.coerceIn(0f, 1f) else 0.5f
    }

    fun setFraction(value: Float): Float {
        val coerced = value.coerceIn(0.02f, 1f)
        val attributes = activity.window.attributes
        attributes.screenBrightness = coerced
        activity.window.attributes = attributes
        return coerced
    }

    fun adjustBy(deltaFraction: Float): Float = setFraction(currentFraction() + deltaFraction)

    fun label(value: Float = currentFraction()): String = stashString(R.string.auto_kr_0177, (value.coerceIn(0f, 1f) * 100f).roundToInt())
}
