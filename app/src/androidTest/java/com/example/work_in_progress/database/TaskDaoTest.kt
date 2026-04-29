package com.example.work_in_progress.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [TaskDao] database operations.
 * Tests are run on a device/emulator using an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        // Create an in-memory database for testing
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTask_shouldAddTaskToDatabase() = runBlocking {
        val task = Task(
            id = 1,
            title = "Buy groceries",
            notes = "Milk, eggs, bread",
            priority = 2,
            due = "2026-05-01",
            remind = true,
            progress = 0,
            target = 1
        )

        taskDao.insertTask(task)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Buy groceries", allTasks[0].title)
    }

    @Test
    fun insertMultipleTasks_shouldAddAllToDatabase() = runBlocking {
        val task1 = Task(title = "Task 1", priority = 1)
        val task2 = Task(title = "Task 2", priority = 2)
        val task3 = Task(title = "Task 3", priority = 3)

        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(3, allTasks.size)
    }

    @Test
    fun insertTask_withNullDueDate_shouldInsertSuccessfully() = runBlocking {
        val task = Task(
            title = "Complete project",
            notes = "No specific deadline",
            due = null,
            priority = 3
        )

        taskDao.insertTask(task)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertTrue(allTasks[0].due == null)
    }

    @Test
    fun updateTask_shouldModifyExistingTask() = runBlocking {
        val originalTask = Task(
            id = 1,
            title = "Original title",
            priority = 1,
            progress = 0
        )
        taskDao.insertTask(originalTask)

        val updatedTask = originalTask.copy(
            title = "Updated title",
            priority = 3,
            progress = 1
        )
        taskDao.updateTask(updatedTask)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Updated title", allTasks[0].title)
        assertEquals(3, allTasks[0].priority)
        assertEquals(1, allTasks[0].progress)
    }

    @Test
    fun deleteTask_shouldRemoveTaskFromDatabase() = runBlocking {
        val task = Task(title = "Task to delete", priority = 1)
        taskDao.insertTask(task)

        val allTasksAfterInsert = taskDao.getAllTasks().first()
        assertEquals(1, allTasksAfterInsert.size)

        // Get the inserted task's ID and delete it
        val insertedTask = allTasksAfterInsert[0]
        taskDao.deleteTask(insertedTask)

        val allTasksAfterDelete = taskDao.getAllTasks().first()
        assertEquals(0, allTasksAfterDelete.size)
    }

    @Test
    fun getAllTasks_shouldReturnTasksInDescendingIdOrder() = runBlocking {
        val task1 = Task(title = "First task")
        val task2 = Task(title = "Second task")
        val task3 = Task(title = "Third task")

        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(3, allTasks.size)
        // Room auto-generates IDs, so most recent insertion should have highest ID
        assertEquals("Third task", allTasks[0].title)
        assertEquals("Second task", allTasks[1].title)
        assertEquals("First task", allTasks[2].title)
    }

    @Test
    fun getAllTasks_emitMultipleValues_whenTasksChange() = runBlocking {
        val emissions = mutableListOf<List<Task>>()
        val flow = taskDao.getAllTasks()

        val task1 = Task(title = "Task 1")
        taskDao.insertTask(task1)
        emissions.add(flow.first())

        val task2 = Task(title = "Task 2")
        taskDao.insertTask(task2)
        emissions.add(flow.first())

        // Verify we got emissions with different sizes
        assertEquals(1, emissions[0].size)
        assertEquals(2, emissions[1].size)
        assertEquals("Task 1", emissions[1].find { it.title == "Task 1" }?.title)
        assertEquals("Task 2", emissions[1].find { it.title == "Task 2" }?.title)
    }

    @Test
    fun insertTask_withOnConflictReplace_shouldReplaceExistingId() = runBlocking {
        val task1 = Task(id = 1, title = "Original title", priority = 1)
        taskDao.insertTask(task1)

        val task2 = Task(id = 1, title = "Replaced title", priority = 2)
        taskDao.insertTask(task2)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Replaced title", allTasks[0].title)
        assertEquals(2, allTasks[0].priority)
    }

    @Test
    fun taskProgressToggle_shouldAlternateCompletionState() = runBlocking {
        val task = Task(title = "Complete me", progress = 0)
        taskDao.insertTask(task)

        var allTasks = taskDao.getAllTasks().first()
        val insertedTask = allTasks[0]
        assertEquals(0, insertedTask.progress)

        // Toggle to complete
        val completedTask = insertedTask.copy(progress = (insertedTask.progress + 1) % 2)
        taskDao.updateTask(completedTask)

        allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks[0].progress)

        // Toggle back to incomplete
        val incompleteTask = allTasks[0].copy(progress = (allTasks[0].progress + 1) % 2)
        taskDao.updateTask(incompleteTask)

        allTasks = taskDao.getAllTasks().first()
        assertEquals(0, allTasks[0].progress)
    }

    @Test
    fun emptyDatabase_shouldReturnEmptyList() = runBlocking {
        val allTasks = taskDao.getAllTasks().first()
        assertEquals(0, allTasks.size)
    }
}
