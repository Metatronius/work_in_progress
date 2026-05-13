/** Immutable value object carrying the user-supplied fields needed to create a new [Task]. */
package com.example.work_in_progress.database

import com.example.work_in_progress.util.Priority

/**
 * Immutable parameter object used to create a new [Task] without exposing the
 * auto-generated [Task.id] or the [Task.created] timestamp to callers.
 *
 * @property title    Short title / name of the task.
 * @property notes    Optional longer description or notes.
 * @property priority Numeric priority level: 0 = Low, 1 = Medium, 2 = High.
 * @property due      Optional due-date string; null if no due date is set.
 * @property remind   Whether the user wants a reminder for this task.
 * @property progress Initial progress value (default 0 = not started).
 */
data class TaskParams(
    val title: String,
    val notes: String = "",
    val priority: Priority = Priority.NONE,
    val due: String? = null,
    val remind: Boolean = false,
    val progress: Int = 0
)