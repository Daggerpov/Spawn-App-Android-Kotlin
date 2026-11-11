package com.danielagapov.spawn.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.util.ActivityStatus
import com.danielagapov.spawn.core.util.Dates
import com.danielagapov.spawn.core.util.Distance
import com.danielagapov.spawn.core.util.UserLocation
import com.danielagapov.spawn.ui.theme.ActivityPalette
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.onest

/** Activity card from the Figma home feed: radial gradient, title, "By X • time", location pill, status pill. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityCard(
    activity: FullFeedActivityDTO,
    colorIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val location by UserLocation.location.collectAsState()
    val status = ActivityStatus.of(activity)
    val shape = RoundedCornerShape(12.dp)
    val people = activity.participantUsers.orEmpty().ifEmpty { listOf(activity.creatorUser) }
    Column(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color.Black.copy(alpha = 0.25f))
            .clip(shape)
            .drawBehind {
                drawRoundRect(ActivityPalette.card(colorIndex, size.width, size.height), cornerRadius = CornerRadius(12.dp.toPx()))
                // soft top highlight (iOS inner shadow white 25%)
                drawRoundRect(
                    androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent), endY = size.height * 0.35f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    listOfNotNull(activity.icon?.takeIf { false }, activity.title ?: "${activity.creatorUser.firstName}'s activity").joinToString(" "),
                    style = onest(20.sp, FontWeight.SemiBold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        append("By ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(if (activity.isSelfOwned == true) "You" else activity.creatorUser.displayName)
                        }
                        append(" • ${Dates.activityTime(activity)}")
                    },
                    style = onest(13.sp), color = Color.White.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            ParticipantsStack(people, size = 32.dp)
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (activity.location != null) {
                GlassPill(Modifier.weight(1f, fill = false)) {
                    Icon(Icons.Outlined.Place, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    val dist = Distance.between(location, activity.location)
                    Text(
                        Distance.shortLocation(activity.location.name) + (dist?.let { " • $it" } ?: ""),
                        style = onest(13.sp, FontWeight.SemiBold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(status)
        }
    }
}

@Composable
fun StatusPill(status: ActivityStatus) {
    Row(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (status.isLive) Palette.HappeningNow else Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(status.text, style = onest(11.sp, FontWeight.SemiBold), color = if (status.isLive) Palette.FigmaNavy else Palette.FigmaNavy)
    }
}

@Composable
fun LiveDot(modifier: Modifier = Modifier) {
    Box(modifier.size(8.dp).clip(CircleShape).background(Palette.Green500))
}
