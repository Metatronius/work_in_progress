package com.example.work_in_progress.database

/**
 * Immutable parameter object used to create a new [Task] without exposing the
 * auto-generated [Task.id] or the [Task.created] timestamp to callers.
 *
 * @property title    Short title / name of the task.
 * @property notes    Optional longer description or notes.
 * @property priority Numeric priority level: 0 = None, 1 = Low, 2 = Medium, 3 = High.
 * @property due      Optional due-date string; null if no due date is set.
 * @property remind   Whether the user wants a reminder for this task.
 * @property progress Initial progress value (default 0 = not started).
 * @property target   Target progress value representing completion (default 1).
 */
data class TaskParams(
    val title: String,
    val notes: String = "",
    val priority: Int = 0,
    val due: String? = null,
    val remind: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)
