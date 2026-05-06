// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress.util

/**
 * Utility object for handling priority-related operations.
 */
 * object DataUtil {
 * val dateFormat = Regex("\\d\\d?/\\d\\d?/\\d\\d\\d\\d")
 * 
/**
 * Converts a string representation of priority to a [Priority] enum.
 *
 * @param priority the string representation of the priority
 * @return the corresponding [Priority] enum value
 */
 * fun getPriority(priority: String): Priority {
 * return Priority.valueOf(priority.trim().uppercase())
 * }
 * 
/**
 * Converts an integer representation of priority to a [Priority] enum.
 *
 * @param priority the integer representation of the priority
 * @return the corresponding [Priority] enum value
 * @throws IllegalArgumentException if the priority is greater than 3
 */
 * fun getPriority(priority: Int): Priority {
 * if (priority > 3) throw IllegalArgumentException("Priority must be between 0 and 3.")
 * return Priority.entries[priority]
 * }
 * 
/**
 * Retrieves the name of the priority as a capitalized string.
 *
 * @param priority the integer representation of the priority
 * @return the name of the corresponding [Priority] enum value, capitalized
 */
 * fun getPriorityName(priority: Int): String {
 * return getPriority(priority).name.lowercase().replaceFirstChar { it.uppercase() }
 * }
 * }
object DataUtil {
    val dateFormat = Regex("\\d\\d?/\\d\\d?/\\d\\d\\d\\d")
    /**
     * Retrieves the corresponding `Priority` enum value based on the provided integer.
     *
     * @param priority an integer representing the priority level, must be between 0 and 3.
     * @return the `Priority` enum value associated with the given integer.
     * @throws IllegalArgumentException if the provided priority is greater than 3.
     */
    fun getPriority(priority: String): Priority {
        return Priority.valueOf(priority.trim().uppercase())
    }
    /**
     * Retrieves the corresponding `Priority` enum value for a given priority level.
     *
     * @param priority An integer representing the priority level, which must be between 0 and 3.
     * @return The `Priority` enum value associated with the specified priority level.
     * @throws IllegalArgumentException if the priority is greater than 3.
     */
    fun getPriority(priority: Int): Priority {
        if (priority > 3) throw IllegalArgumentException("Priority must be between 0 and 3.")
        return Priority.entries[priority]
    }
    /**
     * Retrieves the name of the priority corresponding to the given priority level.
     *
     * The name is formatted to have the first character in uppercase and the rest in lowercase.
     *
     * @param priority the priority level as an integer
     * @return the formatted name of the priority
     */
    fun getPriorityName(priority: Int): String {
        return getPriority(priority).name.lowercase().replaceFirstChar { it.uppercase() }
    }
    /**
     * Processes a date string in the format mm/dd/yyyy.
     *
     * @param date The date string to be processed.
     * @return An error message if the date format is invalid or if the month or day is out of range; otherwise, returns null.
     * @throws NumberFormatException If the date string cannot be split into integers.
     */
    fun getPriorityValue(priority: String): Int {
        return getPriority(priority).ordinal
    }

    /**
     * Processes a date string and validates its format and values.
     *
     * @param date The date string in the format mm/dd/yyyy to be processed.
     * @return An error message if the date is invalid, or null if the date is valid.
     * @throws NumberFormatException If the date string cannot be converted to integers.
     */
    fun processDate(date: String): String? {
        val dateInt = date.split("/").map { it.toInt() }
        if (!dateFormat.matches(date)) { return "Title must match format: mm/dd/yyyy" }

        if(dateInt[0] !in 1..12) { return "Month must be between 1 and 12" }
        if (dateInt[1] !in 1..31) { return "Day must be between 1 and 31" }

        return null
    }
}