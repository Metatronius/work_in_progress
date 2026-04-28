package com.example.work_in_progress

import android.app.Application
import android.content.Context
import com.example.work_in_progress.database.*

val Context.taskRepo get() = (applicationContext as TaskApplication).repository

class TaskApplication : Application() {
    val database by lazy { AppDatabase.Companion.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}