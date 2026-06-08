package com.example.work_in_progress.dtos

import android.os.Parcelable
import com.example.work_in_progress.util.Priority
import kotlinx.parcelize.Parcelize

/**
 * Immutable parameter object used to create a new [Task] without exposing the
 * auto-generated [id] or the [created] timestamp to callers.
 *
 * @property title    Short title / name of the task.
 * @property notes    Optional longer description or notes.
 * @property priority Numeric priority level: 0 = Low, 1 = Medium, 2 = High.
 * @property due      Optional due-date string; null if no due date is set.
 * @property remind   Whether the user wants a reminder for this task.
 * @property progress Initial progress value (default 0 = not started).
 */
data class TaskParams(
    var title: String,
    var notes: String = "",
    var priority: Priority = Priority.NONE,
    val due: String? = null,
    var remind: Boolean = false,
    var progress: Int = 0,
    val target: Int = 1
)