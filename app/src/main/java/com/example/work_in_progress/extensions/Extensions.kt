package com.example.work_in_progress.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.work_in_progress.database.*
import com.example.work_in_progress.taskRepo

fun AppCompatActivity.getTaskViewModel(): TaskViewModel {
    return ViewModelProvider(this, TaskViewModelFactory(this.taskRepo))[TaskViewModel::class.java]
}