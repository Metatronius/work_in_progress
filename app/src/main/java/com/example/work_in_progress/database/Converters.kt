package com.example.work_in_progress.database

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    @TypeConverter
    fun fromTimestamp(value: Long?): String? {
        return value?.let { dateFormat.format(Date(it)) }
    }

    @TypeConverter
    fun dateToTimestamp(date: String?): Long? {
        return try {
            date?.let { dateFormat.parse(it)?.time }
        } catch (e: Exception) {
            null
        }
    }
}
