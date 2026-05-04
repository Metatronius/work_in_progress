package com.example.work_in_progress.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskRepository.allTasks.asLiveData()

    fun addTask(newTask: TaskParams) {
        viewModelScope.launch {
            taskRepository.insert(
                Task(
                    title    = newTask.title,
                    notes    = newTask.notes,
                    priority = newTask.priority,
                    due      = newTask.due,
                    remind   = newTask.remind,
                    progress = newTask.progress,
                    target   = newTask.target
                )
            )
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.update(task.copy(progress = (task.progress + 1) % 2))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}