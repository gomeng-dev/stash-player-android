package gomeng.dev.stashplayer.core.ui.i18n

import android.content.Context
import androidx.annotation.StringRes
import gomeng.dev.stashplayer.R
import java.io.File
import java.util.Locale

@Volatile
private var stashStringContext: Context? = null

private val fallbackStrings: Map<Int, String> by lazy { loadFallbackStrings() }

fun updateStashStringContext(context: Context) {
    stashStringContext = context.applicationContext.withStashAppLanguage(
        StashAppLanguage.fromPersistedValue(null),
    ).takeIf { context === context.applicationContext } ?: context
}

fun stashString(
    @StringRes id: Int,
    vararg formatArgs: Any?,
): String {
    val context = stashStringContext
    val value = if (context != null && formatArgs.isEmpty()) {
        context.getString(id)
    } else if (context != null) {
        context.getString(id, *formatArgs)
    } else {
        fallbackStrings[id] ?: id.toString()
    }
    return if (context == null && formatArgs.isNotEmpty()) {
        runCatching { String.format(Locale.ROOT, value, *formatArgs) }.getOrDefault(value)
    } else {
        value
    }
}

private fun loadFallbackStrings(): Map<Int, String> {
    val idsByName = R.string::class.java.fields
        .mapNotNull { field -> runCatching { field.name to field.getInt(null) }.getOrNull() }
        .toMap()
    val stringsFile = listOf(
        File("app/src/main/res/values/strings.xml"),
        File("src/main/res/values/strings.xml"),
    ).firstOrNull { it.isFile } ?: return emptyMap()
    val xml = stringsFile.readText()
    val stringRegex = Regex(
        pattern = """<string\s+name="([^"]+)"[^>]*>(.*?)</string>""",
        options = setOf(RegexOption.DOT_MATCHES_ALL),
    )
    return stringRegex.findAll(xml)
        .mapNotNull { match ->
            val id = idsByName[match.groupValues[1]] ?: return@mapNotNull null
            id to match.groupValues[2].decodeFallbackXmlString()
        }
        .toMap()
}

private fun String.decodeFallbackXmlString(): String = this
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
    .replace("\\'", "'")
