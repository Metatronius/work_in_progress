package com.example.work_in_progress.util

object DataUtil {
    val dateFormat = Regex("\\d\\d?/\\d\\d?/\\d\\d\\d\\d")

    fun getPriority(priority: String): Priority {
        return Priority.valueOf(priority.trim().uppercase())
    }
    fun getPriority(priority: Int): Priority {
        // TODO: BUG - Line 10-11: Only checks priority > 3 but not priority < 0. Will throw IndexOutOfBoundsException for negative values.
        if (priority > 3) throw IllegalArgumentException("Priority must be between 0 and 3.")
        return Priority.entries[priority]
    }
    fun getPriorityName(priority: Int): String {
        return getPriority(priority).name.lowercase().replaceFirstChar { it.uppercase() }
    }
    fun getPriorityValue(priority: String): Int {
        return getPriority(priority).ordinal
    }

    fun validateDate(date: String?) {
        if (date.isNullOrEmpty()) return;
        // TODO: BUG - Line 21: date.split("/").map { it.toInt() } can throw NumberFormatException. Add try-catch for better error handling.
        val dateInt = date.split("/").map { it.toInt() }
        if (!dateFormat.matches(date)) { throw IllegalArgumentException("Date must match format: mm/dd/yyyy") }

        if(dateInt[0] !in 1..12) { throw IllegalArgumentException("Month must be between 1 and 12") }
        if (dateInt[1] !in 1..31) { throw IllegalArgumentException("Day must be between 1 and 31") }
    }
}
