package com.example.work_in_progress.database
import androidx.room.*
import java.util.Date

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
