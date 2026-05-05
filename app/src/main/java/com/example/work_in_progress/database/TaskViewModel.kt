package com.example.work_in_progress.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskRepository.allTasks.asLiveData()

    fun addTask(newTask: TaskParams) {
        require(newTask.title.isNotBlank() && newTask.title.length in 0..30) { "Title must not be blank or exceed 30 characters." }
        require(newTask.priority in 0..3) { "Priority must be between 0 and 3." }

        viewModelScope.launch {
            taskRepository.insert(
                Task(
                    title = newTask.title,
                    notes = newTask.notes,
                    priority = newTask.priority,
                    due = newTask.due,
                    remind = newTask.remind,
                    progress = newTask.progress,
                    target = 1
                )
            )
        }
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

    /**
     * Updates an existing task in the database with new field values.
     *
     * @param id       The primary key of the task to update.
     * @param title    Updated title.
     * @param notes    Updated notes.
     * @param priority Updated numeric priority (0-3).
     * @param due      Updated due date string, or null.
     * @param remind   Updated reminder flag.
     * @param progress Current progress value.
     * @param target   Target progress value.
     */
    fun editTask(
        id: Int,
        title: String,
        notes: String,
        priority: Int,
        due: String?,
        remind: Boolean,
        progress: Int,
        target: Int
    ) {
        viewModelScope.launch {
            taskRepository.update(
                Task(
                    id       = id,
                    title    = title,
                    notes    = notes,
                    priority = priority,
                    due      = due?.takeIf { it.isNotBlank() },
                    remind   = remind,
                    progress = progress,
                    target   = target
                )
            )
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