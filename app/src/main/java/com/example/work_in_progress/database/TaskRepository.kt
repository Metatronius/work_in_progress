package com.example.work_in_progress.database

import kotlinx.coroutines.flow.Flow

/**
 * Repository mediating access to [Task] data via [TaskDao].
 * Acts as the single source of truth for all task-related data operations,
 * shielding the ViewModel from direct database access.
 *
 * @param taskDao The DAO used to perform database operations.
 */
class TaskRepository(private val taskDao: TaskDao) {
    /** A [Flow] that emits the complete task list whenever the database changes. */
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Inserts a new [task] into the database.
     *
     * @param task The task to persist.
     */
    suspend fun insert(task: Task) = taskDao.insertTask(task)

    /**
     * Updates an existing [task] in the database, matched by [Task.id].
     *
     * @param task The task containing updated field values.
     */
    suspend fun update(task: Task) = taskDao.updateTask(task)

    /**
     * Deletes [task] from the database, matched by [Task.id].
     *
     * @param task The task to remove.
     */
    suspend fun delete(task: Task) = taskDao.deleteTask(task)

    suspend fun getTaskById(id: Int) = taskDao.getTaskById(id)
}