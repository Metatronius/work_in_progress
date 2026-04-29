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

/**
 * Integration tests for [TaskRepository] with a real in-memory Room database.
 * Tests the complete data flow through repository and DAO layers.
 */
@RunWith(AndroidJUnit4::class)
class TaskRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TaskRepository(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addTask_throughRepository_shouldPersistAndEmit() = runBlocking {
        val task = Task(
            title = "Integration test task",
            notes = "Testing repository flow",
            priority = 2,
            due = "2026-06-01",
            remind = false
        )

        repository.insert(task)

        val allTasks = repository.allTasks.first()
        assertEquals(1, allTasks.size)
        assertEquals("Integration test task", allTasks[0].title)
        assertEquals(2, allTasks[0].priority)
    }

    @Test
    fun updateTask_throughRepository_shouldReflectInAllTasks() = runBlocking {
        val originalTask = Task(title = "Original", priority = 1)
        repository.insert(originalTask)

        val allTasks = repository.allTasks.first()
        val insertedTask = allTasks[0]

        val updatedTask = insertedTask.copy(title = "Updated", priority = 3)
        repository.update(updatedTask)

        val updatedAllTasks = repository.allTasks.first()
        assertEquals("Updated", updatedAllTasks[0].title)
        assertEquals(3, updatedAllTasks[0].priority)
    }

    @Test
    fun completeTask_workflow_shouldToggleProgress() = runBlocking {
        val task = Task(title = "Complete me", priority = 2, progress = 0)
        repository.insert(task)

        var allTasks = repository.allTasks.first()
        var currentTask = allTasks[0]
        assertEquals(0, currentTask.progress)

        // Mark complete
        val completedTask = currentTask.copy(progress = (currentTask.progress + 1) % 2)
        repository.update(completedTask)

        allTasks = repository.allTasks.first()
        currentTask = allTasks[0]
        assertEquals(1, currentTask.progress)

        // Mark incomplete
        val incompleteTask = currentTask.copy(progress = (currentTask.progress + 1) % 2)
        repository.update(incompleteTask)

        allTasks = repository.allTasks.first()
        assertEquals(0, allTasks[0].progress)
    }

    @Test
    fun deleteTask_throughRepository_shouldRemoveFromAllTasks() = runBlocking {
        val task1 = Task(title = "Task 1")
        val task2 = Task(title = "Task 2")

        repository.insert(task1)
        repository.insert(task2)

        var allTasks = repository.allTasks.first()
        assertEquals(2, allTasks.size)

        val taskToDelete = allTasks.find { it.title == "Task 1" }!!
        repository.delete(taskToDelete)

        allTasks = repository.allTasks.first()
        assertEquals(1, allTasks.size)
        assertEquals("Task 2", allTasks[0].title)
    }

    @Test
    fun multipleOperations_shouldMaintainDataIntegrity() = runBlocking {
        // Insert 3 tasks
        repeat(3) { i ->
            repository.insert(Task(title = "Task $i", priority = i))
        }

        var allTasks = repository.allTasks.first()
        assertEquals(3, allTasks.size)

        // Update one
        val taskToUpdate = allTasks[0]
        repository.update(taskToUpdate.copy(title = "Updated task", priority = 3))

        // Delete one
        repository.delete(allTasks[1])

        // Verify final state
        allTasks = repository.allTasks.first()
        assertEquals(2, allTasks.size)
        assert(allTasks.any { it.title == "Updated task" })
        assert(allTasks.any { it.title.startsWith("Task") && it.title != "Task 1" })
    }

    @Test
    fun taskWithAllFields_shouldPersistCorrectly() = runBlocking {
        val task = Task(
            title = "Complete task",
            notes = "Full notes field",
            priority = 3,
            due = "2026-12-25",
            remind = true,
            progress = 0,
            target = 1
        )

        repository.insert(task)

        val allTasks = repository.allTasks.first()
        val retrievedTask = allTasks[0]

        assertEquals("Complete task", retrievedTask.title)
        assertEquals("Full notes field", retrievedTask.notes)
        assertEquals(3, retrievedTask.priority)
        assertEquals("2026-12-25", retrievedTask.due)
        assertEquals(true, retrievedTask.remind)
        assertEquals(0, retrievedTask.progress)
        assertEquals(1, retrievedTask.target)
    }

    @Test
    fun taskWithNullOptionalFields_shouldPersistCorrectly() = runBlocking {
        val task = Task(
            title = "Minimal task",
            notes = "",
            due = null,
            remind = false
        )

        repository.insert(task)

        val allTasks = repository.allTasks.first()
        val retrievedTask = allTasks[0]

        assertEquals("Minimal task", retrievedTask.title)
        assertEquals("", retrievedTask.notes)
        assertEquals(null, retrievedTask.due)
        assertEquals(false, retrievedTask.remind)
    }

    @Test
    fun allTasksFlow_shouldEmitInitialAndUpdatedValues() = runBlocking {
        val initialEmission = repository.allTasks.first()
        assertEquals(0, initialEmission.size)

        repository.insert(Task(title = "First"))
        val afterFirstInsert = repository.allTasks.first()
        assertEquals(1, afterFirstInsert.size)

        repository.insert(Task(title = "Second"))
        val afterSecondInsert = repository.allTasks.first()
        assertEquals(2, afterSecondInsert.size)
    }
}
