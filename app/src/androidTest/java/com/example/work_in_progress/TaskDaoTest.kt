/** Instrumented integration tests for [TaskDao] using an in-memory Room database. */
package com.example.work_in_progress

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Instrumented tests for [com.example.work_in_progress.database.TaskDao] using an in-memory
 * Room database so tests are isolated and leave no on-disk state.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var taskDao: TaskDao
    private lateinit var db: AppDatabase

    /** Opens an in-memory database and retrieves the DAO before each test. */
    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        taskDao = db.taskDao()
    }

    /** Closes the in-memory database after each test to release resources. */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /** Inserts a task and verifies it appears in the full task list. */
    @Test
    fun writeTaskAndReadInList() = runBlocking {
        val task = Task(title = "Test Task")
        taskDao.insertTask(task)

        val allTasks = taskDao.getAllTasks().first()
        assert(allTasks[0].title == task.title)
    }

    /** Inserts a task, deletes it, then verifies the task list is empty. */
    @Test
    fun deleteTask() = runBlocking {
        val task = Task(id = 2, title = "Delete Me")
        taskDao.insertTask(task)
        taskDao.deleteTask(task)

        val allTasks = taskDao.getAllTasks().first()

        assert(allTasks.isEmpty())
    }

    /** Inserts a task and verifies it can be retrieved by its primary key. */
    @Test
    fun getTaskById() = runBlocking {
        val task = Task(id = 2, title = "Get Me")
        taskDao.insertTask(task)
        val insertedTask = taskDao.getTaskById(task.id)

        assert(insertedTask !== null)
        assert(insertedTask?.title == task.title)
    }

    /**
     * Tests that changes to a task are persisted correctly in the database.
     *
     * This test inserts a task, updates its title and notes, and verifies that
     * the changes are reflected when fetching the task by its ID.
     */
    @Test
    fun updateTask_persistsChanges() = runBlocking {
        val task = Task(id = 3, title = "Original", notes = "Old notes")
        taskDao.insertTask(task)

        val updated = task.copy(title = "Updated", notes = "New notes")
        taskDao.updateTask(updated)

        val fetched = taskDao.getTaskById(3)
        assertEquals("Updated", fetched?.title)
        assertEquals("New notes", fetched?.notes)
    }

    /**
     * Tests that the `getAllTasks` function returns tasks in descending order by their IDs.
     *
     * This test inserts three tasks with IDs 1, 2, and 3 into the database and verifies that
     * the retrieved list of tasks is ordered from the highest ID to the lowest.
     */
    @Test
    fun getAllTasks_returnsInDescendingIdOrder() = runBlocking {
        taskDao.insertTask(Task(id = 1, title = "First"))
        taskDao.insertTask(Task(id = 2, title = "Second"))
        taskDao.insertTask(Task(id = 3, title = "Third"))

        val allTasks = taskDao.getAllTasks().first()

        assertEquals(3, allTasks[0].id)
        assertEquals(2, allTasks[1].id)
        assertEquals(1, allTasks[2].id)
    }

    /**
     * Tests that inserting a task with an existing ID replaces the original task in the database.
     *
     * This test verifies that when a task is inserted with an ID that already exists,
     * the original task is replaced by the new task.
     */
    @Test
    fun getTaskById_returnsNullForMissingId() = runBlocking {
        val result = taskDao.getTaskById(999)
        assertNull(result)
    }

    /**
     * Tests that inserting a task with the same ID as an existing task replaces the original task.
     *
     * This test verifies that when a task is inserted with an ID that already exists in the database,
     * the original task is replaced by the new task. It checks that the size of the task list remains
     * one and that the title of the task is updated to the new value.
     */
    @Test
    fun insertTask_replacesOnConflict() = runBlocking {
        val task = Task(id = 10, title = "Original")
        taskDao.insertTask(task)

        val replacement = Task(id = 10, title = "Replaced")
        taskDao.insertTask(replacement)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Replaced", allTasks[0].title)
    }
}