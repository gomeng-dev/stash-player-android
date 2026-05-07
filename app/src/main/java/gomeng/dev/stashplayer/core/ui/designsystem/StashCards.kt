package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

/**
 * Visual policy for high-level media dashboard cards.
 *
 * Kept as a pure value so the visual contract can be tested without Compose
 * rendering. Feature screens should use these semantic cards instead of
 * rebuilding raw Material cards with duplicated radius/alpha/spacing values.
 */
data class StashCardPolicy(
    val sectionCornerRadiusDp: Int,
    val heroCornerRadiusDp: Int,
    val sectionPaddingDp: Int,
    val statMinHeightDp: Int,
    val heroMinHeightDp: Int,
    val heroScrimBottomAlpha: Float,
)

fun stashCardPolicy(): StashCardPolicy = StashCardPolicy(
    sectionCornerRadiusDp = StashRadii.Card.value.toInt(),
    heroCornerRadiusDp = StashRadii.Hero.value.toInt(),
    sectionPaddingDp = StashSpacing.CardPadding.value.toInt(),
    statMinHeightDp = 94,
    heroMinHeightDp = 360,
    heroScrimBottomAlpha = StashAlpha.ScrimBottom,
)

data class StashStatCardModel(
    val label: String,
    val value: String,
    val subtitle: String?,
    val accessibilityLabel: String,
    val labelEmphasisWeight: Int,
    val valueEmphasisWeight: Int,
)

fun stashStatCardModel(
    label: String,
    value: String,
    subtitle: String? = null,
    contentDescription: String? = null,
): StashStatCardModel {
    val normalizedLabel = label.trim()
    val normalizedValue = value.trim()
    val normalizedSubtitle = subtitle?.trim()?.takeIf { it.isNotBlank() }
    val fallbackAccessibility = listOf(normalizedLabel, normalizedValue, normalizedSubtitle)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
    return StashStatCardModel(
        label = normalizedLabel,
        value = normalizedValue,
        subtitle = normalizedSubtitle,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
        labelEmphasisWeight = 500,
        valueEmphasisWeight = 800,
    )
}

fun stashHeroMediaMetadataLabels(
    metadataLabels: List<String>,
    showResumeBadge: Boolean,
    maxMetadataBadges: Int = 2,
): List<String> {
    val metadata = metadataLabels
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(maxMetadataBadges.coerceAtLeast(0))
    return if (showResumeBadge) metadata + stashString(R.string.auto_kr_0059) else metadata
}

enum class StashServerStatusTone {
    SetupRequired,
    Error,
    Connected,
}

data class StashServerStatusCardModel(
    val statusLabel: String,
    val detail: String,
    val errorMessage: String?,
    val tone: StashServerStatusTone,
    val primaryActionLabel: String?,
    val secondaryActionLabel: String?,
)

fun stashServerStatusCardModel(
    statusLabel: String,
    detail: String,
    requiresSetup: Boolean,
    hasError: Boolean,
    errorMessage: String? = null,
): StashServerStatusCardModel {
    val tone = when {
        requiresSetup -> StashServerStatusTone.SetupRequired
        hasError -> StashServerStatusTone.Error
        else -> StashServerStatusTone.Connected
    }
    return StashServerStatusCardModel(
        statusLabel = statusLabel.trim(),
        detail = detail.trim(),
        errorMessage = errorMessage?.trim()?.takeIf { it.isNotBlank() },
        tone = tone,
        primaryActionLabel = when (tone) {
            StashServerStatusTone.SetupRequired -> stashString(R.string.auto_kr_0270)
            StashServerStatusTone.Error -> stashString(R.string.auto_kr_0031)
            StashServerStatusTone.Connected -> null
        },
        secondaryActionLabel = when (tone) {
            StashServerStatusTone.Error -> stashString(R.string.auto_kr_0270)
            StashServerStatusTone.SetupRequired,
            StashServerStatusTone.Connected,
            -> null
        },
    )
}

@Composable
fun StashSectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(StashSpacing.CardPadding),
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    StashGlassSurface(
        modifier = modifier
            .then(semanticsModifier)
            .then(clickableModifier),
        cornerRadius = stashCardPolicy().sectionCornerRadiusDp.dp,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun StashStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    contentDescription: String? = null,
) {
    val model = stashStatCardModel(
        label = label,
        value = value,
        subtitle = subtitle,
        contentDescription = contentDescription,
    )
    val policy = stashCardPolicy()

    StashSectionCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = policy.statMinHeightDp.dp),
        contentDescription = model.accessibilityLabel,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = model.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = model.value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (model.subtitle != null) {
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun StashHeroMediaCard(
    title: String,
    subtitle: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailModel: Any? = null,
    thumbnailContentDescription: String? = title,
    metadataLabels: List<String> = emptyList(),
    showResumeBadge: Boolean = false,
    primaryActionLabel: String = stashString(R.string.auto_kr_0039),
) {
    val policy = stashCardPolicy()
    val badgeLabels = stashHeroMediaMetadataLabels(
        metadataLabels = metadataLabels,
        showResumeBadge = showResumeBadge,
    )

    StashMediaCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = policy.heroMinHeightDp.dp),
        cornerRadius = policy.heroCornerRadiusDp.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
        ) {
            if (thumbnailModel != null) {
                AsyncImage(
                    model = thumbnailModel,
                    contentDescription = thumbnailContentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            StashGradientScrim(
                modifier = Modifier.fillMaxSize(),
                topColor = Color.Black.copy(alpha = 0.08f),
                bottomColor = Color.Black.copy(alpha = policy.heroScrimBottomAlpha),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(StashSpacing.SectionGap),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (badgeLabels.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap)) {
                        badgeLabels.forEach { label ->
                            StashMetadataBadge(StashMetadataBadgeModel(label = label))
                        }
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle.ifBlank { stashString(R.string.auto_kr_0383) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(StashRadii.Pill),
                    color = Color.White,
                    contentColor = StashColors.Background,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = StashAlpha.BorderStrong)),
                    modifier = Modifier
                        .widthIn(min = 128.dp)
                        .clickable(onClick = onPrimaryAction)
                        .semantics { contentDescription = primaryActionLabel },
                ) {
                    Text(
                        text = primaryActionLabel,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
fun StashServerStatusCard(
    model: StashServerStatusCardModel,
    onPrimaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onSecondaryAction: (() -> Unit)? = null,
) {
    StashSectionCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stashString(R.string.auto_kr_0384, model.statusLabel),
                style = MaterialTheme.typography.titleMedium,
                color = statusTitleColor(model.tone),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = model.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (model.errorMessage != null) {
                Text(
                    text = stashString(R.string.auto_kr_0385, model.errorMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (model.primaryActionLabel != null && onPrimaryAction != null) {
                    val isError = model.tone == StashServerStatusTone.Error
                    StashSecondaryButton(
                        text = model.primaryActionLabel,
                        onClick = onPrimaryAction,
                        destructive = isError,
                    )
                }
                if (model.secondaryActionLabel != null && onSecondaryAction != null) {
                    StashGhostButton(
                        text = model.secondaryActionLabel,
                        onClick = onSecondaryAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun statusTitleColor(tone: StashServerStatusTone): Color = when (tone) {
    StashServerStatusTone.SetupRequired -> MaterialTheme.colorScheme.primary
    StashServerStatusTone.Error -> MaterialTheme.colorScheme.error
    StashServerStatusTone.Connected -> MaterialTheme.colorScheme.onSurface
}
