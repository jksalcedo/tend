package com.jksalcedo.tend.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.jksalcedo.tend.R

object SocialLinkUtils {

    fun buildProfileUrl(platform: String, handle: String): String {
        val encodedHandle = Uri.encode(handle)
        return when (platform.trim().lowercase()) {
            "twitter", "x" -> "https://x.com/$encodedHandle"
            "instagram" -> "https://instagram.com/$encodedHandle"
            "github" -> "https://github.com/$encodedHandle"
            "facebook" -> "https://facebook.com/$encodedHandle"
            "linkedin" -> "https://linkedin.com/in/$encodedHandle"
            "youtube" -> "https://youtube.com/@$encodedHandle"
            "tiktok" -> "https://tiktok.com/@$encodedHandle"
            "mastodon" -> handle.toMastodonUrl() // handle is "user@instance.social"
            "reddit" -> "https://reddit.com/u/$encodedHandle"
            "threads" -> "https://threads.net/@$encodedHandle"
            "telegram" -> "https://t.me/$encodedHandle"
            "discord" -> "https://discord.com/users/$encodedHandle"
            else -> "https://duckduckgo.com/?q=${Uri.encode("$platform $handle")}"
        }
    }

    // Mastodon handles are "user@instance", not a single fixed domain
    private fun String.toMastodonUrl(): String {
        val parts = trim().removePrefix("@").split("@")
        return if (parts.size == 2) {
            "https://${parts[1]}/@${parts[0]}"
        } else {
            "https://duckduckgo.com/?q=${Uri.encode("mastodon $this")}"
        }
    }

    fun openProfile(context: Context, platform: String, handle: String) {
        val url = buildProfileUrl(platform, handle)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.social_link_no_app_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}