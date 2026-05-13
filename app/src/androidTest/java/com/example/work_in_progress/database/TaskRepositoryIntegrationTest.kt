// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [TaskRepository] using an actual in-memory Room database.
 * Tests verify real database operations including insert, update, delete, and query.
 */
@RunWith(AndroidJUnit4::class)
class TaskRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        taskDao = database.taskDao()
        repository = TaskRepository(taskDao)
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun insert_persistsTaskToDatabase() {
        val task = Task(title = "Integration Test Task")
        runBlocking { repository.insert(task) }

        val allTasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, allTasks.size)
        Assert.assertEquals("Integration Test Task", allTasks[0].title)
    }

    @Test
    fun insert_generatesAutoIncrementId() {
        val task1 = Task(title = "Task 1")
        val task2 = Task(title = "Task 2")

        runBlocking {
            repository.insert(task1)
            repository.insert(task2)
        }

        val allTasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(2, allTasks.size)
        Assert.assertNotEquals(0, allTasks[0].id)
        Assert.assertNotEquals(allTasks[0].id, allTasks[1].id)
    }

    @Test
    fun update_modifiesExistingTask() {
        val task = Task(title = "Original")
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        val insertedTask = tasks[0]

        val updated = insertedTask.copy(title = "Updated")
        runBlocking { repository.update(updated) }

        val allTasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, allTasks.size)
        Assert.assertEquals("Updated", allTasks[0].title)
    }

    @Test
    fun delete_removesTaskFromDatabase() {
        val task = Task(title = "To Delete")
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)

        runBlocking { repository.delete(tasks[0]) }

        val allTasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(0, allTasks.size)
    }

    @Test
    fun getTaskById_returnsCorrectTask() {
        val task = Task(title = "Find Me")
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        val taskId = tasks[0].id

        val result = runBlocking { repository.getTaskById(taskId) }
        Assert.assertNotNull(result)
        Assert.assertEquals("Find Me", result?.title)
    }

    @Test
    fun getTaskById_returnsNullForNonexistent() {
        val result = runBlocking { repository.getTaskById(999) }
        Assert.assertNull(result)
    }

    @Test
    fun allTasks_emitsMultipleTasks() {
        val task1 = Task(title = "Task 1", priority = 1)
        val task2 = Task(title = "Task 2", priority = 2)
        val task3 = Task(title = "Task 3", priority = 0)

        runBlocking {
            repository.insert(task1)
            repository.insert(task2)
            repository.insert(task3)
        }

        val allTasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(3, allTasks.size)
    }

    @Test
    fun update_preservesOtherFields() {
        val task = Task(
            title = "Original",
            notes = "Original notes",
            priority = 2,
            due = "5/15/2025",
            remind = true,
            progress = 0
        )
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        val updated = tasks[0].copy(title = "New Title")
        runBlocking { repository.update(updated) }

        val result = runBlocking { repository.getTaskById(tasks[0].id) }
        Assert.assertEquals("New Title", result?.title)
        Assert.assertEquals("Original notes", result?.notes)
        Assert.assertEquals(2, result?.priority)
        Assert.assertEquals("5/15/2025", result?.due)
        Assert.assertTrue(result?.remind ?: false)
    }

    @Test
    fun delete_multipleTasksIndependently() {
        val task1 = Task(title = "Keep")
        val task2 = Task(title = "Delete")
        val task3 = Task(title = "Keep Also")

        runBlocking {
            repository.insert(task1)
            repository.insert(task2)
            repository.insert(task3)
        }

        val tasks = runBlocking { repository.allTasks.first() }
        val toDelete = tasks.find { it.title == "Delete" }
        Assert.assertNotNull(toDelete)

        runBlocking { repository.delete(toDelete!!) }

        val remaining = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(2, remaining.size)
        Assert.assertTrue(remaining.any { it.title == "Keep" })
        Assert.assertTrue(remaining.any { it.title == "Keep Also" })
        Assert.assertFalse(remaining.any { it.title == "Delete" })
    }

    @Test
    fun insert_withAllFields_persistsCorrectly() {
        val task = Task(
            title = "Full Task",
            notes = "Full notes",
            priority = 3,
            due = "12/31/2025",
            remind = true,
            progress = 1,
            target = 1
        )
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        val result = tasks[0]

        Assert.assertEquals("Full Task", result.title)
        Assert.assertEquals("Full notes", result.notes)
        Assert.assertEquals(3, result.priority)
        Assert.assertEquals("12/31/2025", result.due)
        Assert.assertTrue(result.remind)
        Assert.assertEquals(1, result.progress)
        Assert.assertEquals(1, result.target)
    }
}
