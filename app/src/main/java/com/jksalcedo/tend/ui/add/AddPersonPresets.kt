package com.jksalcedo.tend.ui.add

import androidx.annotation.StringRes
import com.jksalcedo.tend.R
import com.jksalcedo.tend.domain.model.EventType

/**
 * Pre-made social platforms offered in the "Add Social Link" dialog.
 *
 * [platform] is the value stored on the [com.jksalcedo.tend.domain.model.SocialLink] and must
 * stay in sync with the platform matching in
 * [com.jksalcedo.tend.utils.SocialLinkUtils.buildProfileUrl].
 */
data class SocialPlatformPreset(
    val platform: String,
    @StringRes val labelRes: Int,
    @StringRes val handleHintRes: Int
)

val socialPlatformPresets = listOf(
    SocialPlatformPreset("GitHub", R.string.social_platform_github, R.string.add_person_handle_username),
    SocialPlatformPreset("X", R.string.social_platform_x, R.string.add_person_handle_username),
    SocialPlatformPreset("Instagram", R.string.social_platform_instagram, R.string.add_person_handle_username),
    SocialPlatformPreset("Facebook", R.string.social_platform_facebook, R.string.add_person_handle_username),
    SocialPlatformPreset("LinkedIn", R.string.social_platform_linkedin, R.string.add_person_handle_username),
    SocialPlatformPreset("YouTube", R.string.social_platform_youtube, R.string.add_person_handle_username),
    SocialPlatformPreset("TikTok", R.string.social_platform_tiktok, R.string.add_person_handle_username),
    SocialPlatformPreset("Mastodon", R.string.social_platform_mastodon, R.string.add_person_handle_mastodon),
    SocialPlatformPreset("Reddit", R.string.social_platform_reddit, R.string.add_person_handle_username),
    SocialPlatformPreset("Threads", R.string.social_platform_threads, R.string.add_person_handle_username),
    SocialPlatformPreset("Telegram", R.string.social_platform_telegram, R.string.add_person_handle_username),
    SocialPlatformPreset("Discord", R.string.social_platform_discord, R.string.add_person_handle_username),
    SocialPlatformPreset("Website", R.string.social_platform_website, R.string.add_person_handle_website)
)

/** Pre-made important dates offered in the "Add Event" dialog. */
data class EventPreset(
    @StringRes val labelRes: Int,
    val type: EventType
)

val eventPresets = listOf(
    EventPreset(R.string.event_preset_birthday, EventType.BIRTHDAY),
    EventPreset(R.string.event_preset_anniversary, EventType.ANNIVERSARY),
    EventPreset(R.string.event_preset_wedding, EventType.ANNIVERSARY),
    EventPreset(R.string.event_preset_work_anniversary, EventType.ANNIVERSARY),
    EventPreset(R.string.event_preset_graduation, EventType.OTHER),
    EventPreset(R.string.event_preset_first_met, EventType.OTHER),
    EventPreset(R.string.event_preset_memorial, EventType.OTHER)
)

/** Best-effort event type for labels the user typed by hand. */
fun inferEventType(label: String): EventType = when {
    label.contains("birthday", ignoreCase = true) ||
        label.contains("bday", ignoreCase = true) -> EventType.BIRTHDAY

    label.contains("anniversary", ignoreCase = true) ||
        label.contains("wedding", ignoreCase = true) -> EventType.ANNIVERSARY

    else -> EventType.OTHER
}
