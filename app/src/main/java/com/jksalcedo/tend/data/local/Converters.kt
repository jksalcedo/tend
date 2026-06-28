package com.jksalcedo.tend.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSocialLinkList(value: List<SocialLink>?): String {
        return gson.toJson(value ?: emptyList<SocialLink>())
    }

    @TypeConverter
    fun toSocialLinkList(value: String): List<SocialLink> {
        val type = object : TypeToken<List<SocialLink>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromPersonEventList(value: List<PersonEvent>?): String {
        return gson.toJson(value ?: emptyList<PersonEvent>())
    }

    @TypeConverter
    fun toPersonEventList(value: String): List<PersonEvent> {
        val type = object : TypeToken<List<PersonEvent>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromNoteList(value: List<Note>?): String {
        return gson.toJson(value ?: emptyList<Note>())
    }

    @TypeConverter
    fun toNoteList(value: String): List<Note> {
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
