package com.example.work_in_progress.database
import androidx.room.*
import java.util.Date

/**
 * Room entity representing a single task stored in the "tasks" table.
 *
 * @property id       Auto-generated primary key.
 * @property title    Short title / name of the task.
 * @property notes    Optional longer description or notes for the task.
 * @property priority Numeric priority level: 0 = None, 1 = Low, 2 = Medium, 3 = High.
 * @property created  String timestamp recording when the task was created.
 * @property due      Optional due-date string; null if no due date is set.
 * @property remind   Whether the user wants a reminder for this task.
 * @property progress Current progress value (0 = incomplete, 1 = complete).
 * @property target   Target progress value that represents full completion (default 1).
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String = "",
    val priority: Int = 0,
    val created: String = Date().toString(),
    val due: String? = null,
    val remind: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)
