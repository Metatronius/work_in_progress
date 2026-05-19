/** ViewModel and its factory for managing [Task] data exposed to the UI layer. */
package com.example.work_in_progress.database

import androidx.lifecycle.*
import com.example.work_in_progress.util.DataUtil
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes task data as [LiveData] and provides coroutine-backed
 * operations for adding, deleting, and completing tasks.
 *
 * @param taskRepository The repository used to perform data operations.
 */
class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    /** Live list of all tasks, ordered by most recently inserted first. */
    val allTasks: LiveData<List<Task>> = taskRepository.allTasks.asLiveData()

    /**
     * Validates [newTask] and inserts it into the database.
     * Throws [IllegalArgumentException] if the title is blank, exceeds 30 characters,
     * or the priority is outside the range 0–3.
     *
     * @param newTask The parameter object containing the task fields to persist.
     */
    fun addTask(newTask: TaskParams, onInserted: ((Int) -> Unit)? = null) {
        require(newTask.title.isNotBlank() && newTask.title.length in 0..30) { "Title must not be blank or exceed 30 characters." }
        if (newTask.due !== null)
            DataUtil.validateDate(newTask.due)

        viewModelScope.launch {
            val insertedId = taskRepository.insert(
                Task(
                    title = newTask.title,
                    notes = newTask.notes,
                    priority = newTask.priority.ordinal,
                    due = newTask.due,
                    remind = newTask.remind,
                    progress = newTask.progress,
                    target = 1
                )
            )
            require(insertedId in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                "Inserted task id out of Int range: $insertedId"
            }
            onInserted?.invoke(insertedId.toInt())
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

/**
 * [ViewModelProvider.Factory] that constructs a [TaskViewModel] with the required
 * [TaskRepository] dependency.
 *
 * @param repository The repository to inject into the created [TaskViewModel].
 */
class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    /**
     * Creates a new [TaskViewModel] instance if [modelClass] is assignable from it.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A newly constructed [TaskViewModel] cast to [T].
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
