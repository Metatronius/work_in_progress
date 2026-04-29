package com.example.work_in_progress.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch

/**
 * ViewModel exposing task data and operations to the UI layer.
 * Survives configuration changes and keeps the UI free of direct database access.
 *
 * @param taskRepository The repository used for all task data operations.
 */
class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    /** LiveData list of all tasks; automatically updated whenever the database changes. */
    val allTasks: LiveData<List<Task>> = taskRepository.allTasks.asLiveData()

    /**
     * Persists a new task built from [newTask] parameters to the database.
     * All fields from [TaskParams] are mapped to the corresponding [Task] fields.
     *
     * @param newTask Parameters describing the task to create.
     */
    fun addTask(newTask: TaskParams) = viewModelScope.launch {
        taskRepository.insert(
            Task(
                title = newTask.title,
                notes = newTask.notes,
                priority = newTask.priority,
                due = newTask.due,
                remind = newTask.remind,
                progress = newTask.progress,
                target = newTask.target
            )
        )
    }

    /**
     * Delete a task by its [id] from the database.
     *
     * @param id The primary key of the task to delete.
     */
    fun deleteTaskById(id: Int) = viewModelScope.launch {
        val targetTask = taskRepository.getTaskById(id)
        targetTask?.let {
            taskRepository.delete(targetTask)
        }
    }

    /**
     * Deletes [task] from the database.
     *
     * @param task The task to remove.
     */
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.delete(task)
    }

    /**
     * Toggles the completion state of [task] between 0 (incomplete) and 1 (complete)
     * and persists the change to the database.
     *
     * @param task The task whose progress should be toggled.
     */
    fun completeTask(task: Task) = viewModelScope.launch {
            taskRepository.update(task.copy(progress = (task.progress + 1) % 2))
        }
    }

/**
 * [ViewModelProvider.Factory] for creating [TaskViewModel] instances with the
 * required [TaskRepository] dependency injected.
 *
 * @param repository The repository to inject into the created [TaskViewModel].
 */
class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the requested ViewModel class.
     *
     * @param T          The type of ViewModel to create.
     * @param modelClass The [Class] of the ViewModel to instantiate.
     * @return A new [TaskViewModel] cast to [T].
     * @throws IllegalArgumentException if [modelClass] is not [TaskViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}