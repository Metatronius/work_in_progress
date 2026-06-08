/** Room DAO interface declaring all SQL operations for the [com.example.work_in_progress.entities.Task] entity. */
package com.example.work_in_progress.database
import androidx.room.*
import com.example.work_in_progress.entities.Task
import kotlinx.coroutines.flow.Flow

/** Data Access Object for [com.example.work_in_progress.entities.Task] database operations. */
@Dao
interface TaskDao {
    /**
     * Returns a [Flow] that emits the full list of tasks.
     * Tasks are ordered by newest first (descending ID).
     */
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Returns the [Task] with the given [id], or null if no such task exists.
     *
     * @param id The primary key of the task to look up.
     */
    @Query(value = "SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    /**
     * Inserts [task] into the database. If a row with the same primary key already
     * exists it is replaced.
     *
     * @param task The task to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /**
     * Updates an existing [task] row in the database, matched by primary key.
     *
     * @param task The task containing updated field values.
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Deletes [task] from the database, matched by primary key.
     *
     * @param task The task to remove.
     */
    @Delete
    suspend fun deleteTask(task: Task)
}
