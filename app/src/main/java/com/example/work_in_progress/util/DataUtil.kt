package com.example.work_in_progress.util

import com.example.work_in_progress.dtos.TaskParams

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

    fun validateDate(date: String) {
        val dateInt = date.split("/").map { it.toInt() }
        require(dateFormat.matches(date)) { "Title must match format: mm/dd/yyyy" }

        require(dateInt[0] in 1..12) { "Month must be between 1 and 12" }
        require(dateInt[1] in 1..31) { "Day must be between 1 and 31" }
    }

    fun validateTitle(title: String) {
        require(title.isNotBlank() && title.length in 0..30) { "Title must not be blank or exceed 30 characters." }
    }

    fun validateTask(task: TaskParams) {
        if (task.due != null)
            validateDate(task.due)
        validateTitle(task.title)
    }
}
