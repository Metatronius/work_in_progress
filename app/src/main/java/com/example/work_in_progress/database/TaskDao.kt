package com.example.work_in_progress.database
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** Data Access Object for [Task] database operations. */
@Dao
interface TaskDao {
    /**
     * Returns a [Flow] that emits the full list of tasks ordered by most recently
     * inserted first. The Flow re-emits whenever the underlying table changes.
     */
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query(value = "SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Int): Task?

    /**
     * Inserts [task] into the database. If a row with the same primary key already
     * exists it is replaced.
     *
     * @param task The task to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

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