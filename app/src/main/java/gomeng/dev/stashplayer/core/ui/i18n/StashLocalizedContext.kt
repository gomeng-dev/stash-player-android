package gomeng.dev.stashplayer.core.ui.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

fun Context.withStashAppLanguage(language: StashAppLanguage): Context {
    val localeTag = language.localeTag ?: return this
    val locale = Locale.forLanguageTag(localeTag)
    val configuration = Configuration(resources.configuration).apply {
        setLocales(LocaleList(locale))
    }
    return createConfigurationContext(configuration)
}
