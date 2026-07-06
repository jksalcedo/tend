package com.jksalcedo.tend.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object SocialIconUtils {

    private val platformIcons: Map<String, ImageVector> = mapOf(
        "github" to Icons.Default.Code,
        "twitter" to Icons.Default.AlternateEmail,
        "x" to Icons.Default.AlternateEmail,
        "instagram" to Icons.Default.CameraAlt,
        "linkedin" to Icons.Default.Work,
        "discord" to Icons.AutoMirrored.Filled.Send,
        "telegram" to Icons.AutoMirrored.Filled.Send,
        "whatsapp" to Icons.AutoMirrored.Filled.Send,
        "signal" to Icons.AutoMirrored.Filled.Send,
        "chat" to Icons.AutoMirrored.Filled.Send,
        "website" to Icons.Default.Language,
        "web" to Icons.Default.Language,
        "portfolio" to Icons.Default.Language,
        "facebook" to Icons.Default.Public,
        "youtube" to Icons.Default.PlayCircle,
        "tiktok" to Icons.Default.MusicNote,
        "mastodon" to Icons.Default.Forum,
        "reddit" to Icons.Default.Forum,
        "threads" to Icons.Default.AlternateEmail,
    )

    fun getIcon(platform: String): ImageVector =
        platformIcons[platform.trim().lowercase()] ?: Icons.Default.Link
}