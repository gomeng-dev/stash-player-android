package gomeng.dev.stashplayer.core.network

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

internal object DeveloperPromoPreferences {
    val launchCount = intPreferencesKey("developer_promo_process_launch_count")
    val homeDismissed = booleanPreferencesKey("developer_promo_home_dismissed")

    fun recordProcessLaunch(prefs: MutablePreferences) {
        val count = prefs[launchCount] ?: 0
        prefs[launchCount] = count.coerceIn(0, Int.MAX_VALUE - 1) + 1
    }

    fun dismissHome(prefs: MutablePreferences) {
        prefs[homeDismissed] = true
    }

    fun isHomeVisible(prefs: Preferences): Boolean =
        (prefs[launchCount] ?: 0) >= 5 && prefs[homeDismissed] != true
}
