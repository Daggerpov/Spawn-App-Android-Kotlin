package com.danielagapov.spawn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.SubcomposeAsyncImage
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

private val avatarColors = listOf(
    Color(0xFF6B81FB), Color(0xFFED64A6), Color(0xFF1AB979), Color(0xFF1D85E7), Color(0xFFFD4E4C), Color(0xFF8A53FB),
)

/** Profile picture with an initials fallback (local/new users often have no picture). */
@Composable
fun Avatar(
    url: String?,
    name: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
) {
    val initials = (name ?: "?").trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }
    val bg = avatarColors[((name ?: "").hashCode() and 0x7fffffff) % avatarColors.size]
    val base = modifier
        .size(size)
        .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
        .clip(CircleShape)
    val fallback: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
            Text(initials.ifEmpty { "?" }, style = onest((size.value * 0.38f).sp, FontWeight.SemiBold), color = Color.White)
        }
    }
    if (url.isNullOrBlank()) {
        Box(base) { fallback() }
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = base,
            loading = { Box(Modifier.fillMaxSize().background(SpawnTheme.colors.surfaceMuted)) },
            error = { fallback() },
        )
    }
}

@Composable
fun UserAvatar(user: BaseUserDTO, size: Dp, modifier: Modifier = Modifier, border: BorderStroke? = null) =
    Avatar(user.profilePicture, user.name ?: user.username, size, modifier, border)

/** Overlapping participant avatars with a "+N" bubble (Figma feed card / drawer). */
@Composable
fun ParticipantsStack(users: List<BaseUserDTO>, size: Dp = 34.dp, max: Int = 2, ringColor: Color = Color.White) {
    val shown = users.take(max)
    val extra = users.size - shown.size
    Row(horizontalArrangement = Arrangement.spacedBy(-(size * 0.35f))) {
        shown.forEachIndexed { i, u ->
            UserAvatar(u, size, Modifier.zIndex((max - i).toFloat()), BorderStroke(1.5.dp, ringColor))
        }
        if (extra > 0) {
            Box(
                Modifier.size(size).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$extra", style = onest((size.value * 0.36f).sp, FontWeight.SemiBold), color = Palette.FigmaNavy)
            }
        }
    }
}

/** "Spawn in! (>)" style section header with the chevron circle from Figma. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier.then(if (onClick != null) Modifier.clickable(remember { MutableInteractionSource() }, null, onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = onest(20.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(SpawnTheme.colors.chevronCircle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Palette.FigmaNavy, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    brush: Brush? = null,
    leading: ImageVector? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (brush != null && enabled) Modifier.background(brush, shape) else Modifier.background(
        if (enabled) Palette.Indigo500 else Palette.Gray200, shape
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(if (enabled) 6.dp else 0.dp, shape, ambientColor = Palette.Indigo500, spotColor = Palette.Indigo500)
            .clip(shape)
            .then(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) {
                    Icon(leading, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text, style = onest(17.sp, FontWeight.SemiBold), color = Color.White)
            }
        }
    }
}

@Composable
fun OutlinePillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Palette.Indigo500) {
    Box(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, color, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = onest(15.sp, FontWeight.SemiBold), color = color)
    }
}

@Composable
fun SpawnTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    val c = SpawnTheme.colors
    Column(modifier) {
        if (label != null) {
            Text(label, style = onest(14.sp, FontWeight.Medium), color = c.text)
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = onest(16.sp), color = c.placeholder) },
            singleLine = singleLine,
            minLines = minLines,
            textStyle = onest(16.sp).copy(color = c.text),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.input,
                unfocusedContainerColor = c.input,
                focusedBorderColor = Palette.Indigo500,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Palette.Red600,
                errorContainerColor = c.input,
                cursorColor = Palette.Indigo500,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = onest(13.sp), color = Palette.Red600)
        }
    }
}

/** Centered title header with a back chevron (iOS UnifiedNavigationHeader). */
@Composable
fun SpawnTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SpawnTheme.colors.heading)
            }
        }
        Text(
            title,
            style = onest(20.sp, FontWeight.SemiBold),
            color = SpawnTheme.colors.heading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
        )
        if (trailing != null) {
            Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = onest(22.sp, FontWeight.SemiBold), color = SpawnTheme.colors.heading, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = onest(15.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Palette.Indigo500, strokeWidth = 2.5.dp)
    }
}

@Composable
fun ErrorBanner(message: String, onRetry: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF5F5))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = onest(14.sp), color = Palette.Red600, modifier = Modifier.weight(1f))
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text("Retry", style = onest(14.sp, FontWeight.SemiBold), color = Palette.Red600) }
        }
    }
}

/** Small white translucent pill used on gradient cards ("Location • 500m away", "Happening Now"). */
@Composable
fun GlassPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
