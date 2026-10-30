package com.nexrblx.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NexBg = Color(0xFF07040F)
val NexCard = Color(0xFF130B24)
val NexCard2 = Color(0xFF1B1033)
val NexLine = Color(0xFF2A1A4A)
val NexPurple = Color(0xFFA855F7)
val NexNeon = Color(0xFFD946EF)
val NexText = Color(0xFFF4EEFF)
val NexMuted = Color(0xFF9D8CC0)

@Composable
fun NexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexPurple,
            secondary = NexNeon,
            background = NexBg,
            surface = NexCard,
            onBackground = NexText,
            onSurface = NexText
        ),
        content = content
    )
}
