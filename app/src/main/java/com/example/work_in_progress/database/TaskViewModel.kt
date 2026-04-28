package com.example.work_in_progress.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun addTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.insertTask(newTask)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(progress = (task.progress + 1) % 2))
        }
    }
}

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}