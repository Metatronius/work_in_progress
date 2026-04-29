package com.example.work_in_progress.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.work_in_progress.database.*
import com.example.work_in_progress.taskRepo

/**
 * Creates and returns a [TaskViewModel] scoped to this [AppCompatActivity],
 * injecting the application's [com.example.work_in_progress.database.TaskRepository]
 * via [TaskViewModelFactory].
 *
 * @return The [TaskViewModel] instance associated with this activity's lifecycle.
 */
fun AppCompatActivity.getTaskViewModel(): TaskViewModel {
    return ViewModelProvider(this, TaskViewModelFactory(this.taskRepo))[TaskViewModel::class.java]
}