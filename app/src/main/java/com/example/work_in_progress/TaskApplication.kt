package com.example.work_in_progress

import android.app.Application
import android.content.Context
import com.example.work_in_progress.database.*

/** Convenience extension to access the [TaskRepository] from any [Context]. */
val Context.taskRepo get() = (applicationContext as TaskApplication).repository

/**
 * Application-level entry point. Lazily initializes the Room [AppDatabase]
 * and the [TaskRepository] singleton used throughout the app.
 */
class TaskApplication : Application() {
    /** The Room database singleton for this application. */
    val database by lazy { AppDatabase.getDatabase(this) }
    /** The repository providing access to task data. */
    val repository by lazy { TaskRepository(database.taskDao()) }
}