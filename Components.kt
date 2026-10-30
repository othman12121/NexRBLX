package com.nexrblx.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// ---------------------------------------------------------------- colors
val NexBorder = Color(0xFF2A1A4A)
val NexCodeBg = Color(0xFF0C0718)
val NexComment = Color(0xFF6B5B8F)
val NexString = Color(0xFF86EFAC)
val NexNumber = Color(0xFFFBBF24)
val NexKeyword = Color(0xFFF472B6)
val NexBuiltin = Color(0xFF67E8F9)
val NexGreen = Color(0xFF4ADE80)
val NexRed = Color(0xFFF87171)
val NexAmber = Color(0xFFFBBF24)

val NexGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFD946EF)))

// ---------------------------------------------------------------- language
val LocalAr = compositionLocalOf { false }

@Composable
fun tr(en: String, ar: String): String = if (LocalAr.current) ar else en

// ---------------------------------------------------------------- helpers
fun openUrl(ctx: Context, url: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, "No browser found", Toast.LENGTH_SHORT).show()
    }
}

fun shareText(ctx: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        ctx.startActivity(Intent.createChooser(i, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
    }
}

@Composable
fun Modifier.bounceClick(onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(120), label = "bounce")
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

// ---------------------------------------------------------------- logo
@Composable
fun NexLogo(size: Int = 26) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Nex", fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = NexText)
        Text("RBLX", fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = NexNeon)
    }
}

// ---------------------------------------------------------------- header
@Composable
fun Header(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = NexText)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NexText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NexText, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                fontSize = 13.sp,
                color = NexPurple,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

// ---------------------------------------------------------------- chips / tags
@Composable
fun NexChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) NexPurple.copy(alpha = 0.28f) else NexCard, tween(180), label = "chipbg")
    val border = if (selected) NexPurple else NexBorder
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 13.sp, color = if (selected) NexText else NexMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun Tag(text: String, color: Color = NexPurple) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatusTag(status: String) {
    when (status) {
        "new" -> Tag(tr("NEW", "جديد"), NexGreen)
        "expired" -> Tag(tr("EXPIRED", "منتهي"), NexRed)
        else -> Tag(tr("ACTIVE", "فعّال"), NexPurple)
    }
}

// ---------------------------------------------------------------- buttons
@Composable
fun NexButton(text: String, modifier: Modifier = Modifier, filled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .clip(shape)
            .then(
                if (filled) Modifier.background(NexGradient)
                else Modifier.background(NexCard).border(1.dp, NexPurple.copy(alpha = 0.6f), shape)
            )
            .bounceClick(onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
    }
}

// ---------------------------------------------------------------- thumbnail
@Composable
fun Thumb(url: String, name: String, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3B1470), Color(0xFF8B2FC9)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).uppercase(),
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun NexCardBox(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val base = modifier
        .clip(shape)
        .background(NexCard)
        .border(1.dp, NexBorder, shape)
    Box(if (onClick != null) base.bounceClick(onClick) else base) { content() }
}

// ---------------------------------------------------------------- Lua viewer
private val luaKeywords = setOf(
    "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "if", "in",
    "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while", "continue"
)
private val luaBuiltins = setOf(
    "game", "workspace", "script", "wait", "task", "print", "warn", "Instance", "Vector3", "CFrame",
    "Color3", "Enum", "pairs", "ipairs", "tostring", "tonumber", "pcall", "math", "string", "table",
    "require", "os", "tick", "UDim2", "BrickColor"
)
private val luaRegex = Regex(
    """(--[^\n]*)|("(?:\\.|[^"\\\n])*"|'(?:\\.|[^'\\\n])*')|(\b\d+(?:\.\d+)?\b)|(\b[A-Za-z_][A-Za-z0-9_]*\b)"""
)

fun highlightLua(code: String): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in luaRegex.findAll(code)) {
        if (m.range.first > last) append(code.substring(last, m.range.first))
        val t = m.value
        val color: Color? = when {
            m.groups[1] != null -> NexComment
            m.groups[2] != null -> NexString
            m.groups[3] != null -> NexNumber
            t in luaKeywords -> NexKeyword
            t in luaBuiltins -> NexBuiltin
            else -> null
        }
        if (color != null) {
            withStyle(SpanStyle(color = color)) { append(t) }
        } else {
            append(t)
        }
        last = m.range.last + 1
    }
    if (last < code.length) append(code.substring(last))
}

@Composable
fun LuaViewer(code: String, modifier: Modifier = Modifier) {
    val styled = remember(code) { highlightLua(code.trimEnd()) }
    val numbers = remember(code) { (1..code.trimEnd().lines().size).joinToString("\n") }
    val shape = RoundedCornerShape(14.dp)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier
                .clip(shape)
                .background(NexCodeBg)
                .border(1.dp, NexBorder, shape)
                .padding(12.dp)
        ) {
            Text(
                numbers,
                color = NexMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 10.dp)
            )
            Box(
                Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    styled,
                    color = NexText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    softWrap = false
                )
            }
        }
    }
}

/** Copy button that flips to "Copied" for 1.5 s. */
@Composable
fun CopyButton(text: String, label: String, modifier: Modifier = Modifier, filled: Boolean = true) {
    val clip = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    NexButton(
        text = if (copied) tr("Copied ✓", "تم النسخ ✓") else label,
        modifier = modifier,
        filled = filled
    ) {
        clip.setText(AnnotatedString(text))
        copied = true
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text, color = NexMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

val ScreenPadding = PaddingValues(horizontal = 16.dp)
