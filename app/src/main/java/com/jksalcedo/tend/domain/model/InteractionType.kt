package com.jksalcedo.tend.domain.model

enum class InteractionType(val displayName: String) {
    CALL("Call"),
    MESSAGE("Message"),
    MEETUP("In Person"),
    VIDEO_CALL("Video Call"),
    OTHER("Other");

    companion object {
        fun fromString(type: String?): InteractionType? {
            if (type == null) return null
            return try {
                valueOf(type)
            } catch (e: Exception) {
                OTHER
            }
        }
    }
}
