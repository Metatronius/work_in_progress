package com.example.work_in_progress.database

data class TaskParams(
    val title: String,
    val notes: String = "",
    val priority: Int = 0,
    val due: String? = null,
    val remind: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)
