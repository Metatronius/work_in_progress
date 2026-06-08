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
import com.example.work_in_progress.entities.Task

/**
 * Instrumented tests for [TaskDao] directly on database layer.
 * Tests verify Room/SQLite behavior including constraints and persistence.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        taskDao = database.taskDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertTask_persistsData() {
        val task = Task(title = "Test Task")
        runBlocking { taskDao.insertTask(task) }

        val allTasks = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertEquals(1, allTasks.size)
        Assert.assertEquals("Test Task", allTasks[0].title)
    }

    @Test
    fun insertTask_generatesId() {
        val task = Task(title = "Test")
        runBlocking { taskDao.insertTask(task) }

        val all = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertNotEquals(0, all[0].id)
    }

    @Test
    fun updateTask_modifiesExisting() {
        val task = Task(title = "Original")
        runBlocking { taskDao.insertTask(task) }

        val inserted = runBlocking { taskDao.getAllTasks().first()[0] }
        val updated = inserted.copy(title = "Modified")
        runBlocking { taskDao.updateTask(updated) }

        val result = runBlocking { taskDao.getAllTasks().first()[0] }
        Assert.assertEquals("Modified", result.title)
    }

    @Test
    fun deleteTask_removesData() {
        val task = Task(title = "Delete Me")
        runBlocking { taskDao.insertTask(task) }

        val inserted = runBlocking { taskDao.getAllTasks().first()[0] }
        runBlocking { taskDao.deleteTask(inserted) }

        val remaining = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertEquals(0, remaining.size)
    }

    @Test
    fun getTaskById_retrieves() {
        val task = Task(title = "Find Me")
        runBlocking { taskDao.insertTask(task) }

        val inserted = runBlocking { taskDao.getAllTasks().first()[0] }
        val result = runBlocking { taskDao.getTaskById(inserted.id) }

        Assert.assertNotNull(result)
        Assert.assertEquals("Find Me", result?.title)
    }

    @Test
    fun getTaskById_returnsNull_whenNotFound() {
        val result = runBlocking { taskDao.getTaskById(999) }
        Assert.assertNull(result)
    }

    @Test
    fun getAllTasks_returnsEmptyInitially() {
        val tasks = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertEquals(0, tasks.size)
    }

    @Test
    fun getAllTasks_emitsBothTasksAfterInsert() {
        val task1 = Task(title = "First")
        val task2 = Task(title = "Second")
        runBlocking {
            taskDao.insertTask(task1)
            taskDao.insertTask(task2)
        }

        val all = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertEquals(2, all.size)
    }

    @Test
    fun task_preservesAllFields() {
        val task = Task(
            title = "Full",
            notes = "Notes",
            priority = 2,
            due = "5/20/2025",
            remind = true,
            progress = 1,
            target = 1
        )
        runBlocking { taskDao.insertTask(task) }

        val inserted = runBlocking { taskDao.getAllTasks().first()[0] }
        Assert.assertEquals("Full", inserted.title)
        Assert.assertEquals("Notes", inserted.notes)
        Assert.assertEquals(2, inserted.priority)
        Assert.assertEquals("5/20/2025", inserted.due)
        Assert.assertTrue(inserted.remind)
        Assert.assertEquals(1, inserted.progress)
        Assert.assertEquals(1, inserted.target)
    }

    @Test
    fun task_progressToggle_works() {
        val task = Task(title = "Toggle", progress = 0)
        runBlocking { taskDao.insertTask(task) }

        val inserted = runBlocking { taskDao.getAllTasks().first()[0] }
        val toggled = inserted.copy(progress = (inserted.progress + 1) % 2)
        runBlocking { taskDao.updateTask(toggled) }

        val result = runBlocking { taskDao.getAllTasks().first()[0] }
        Assert.assertEquals(1, result.progress)
    }

    @Test
    fun multipleDeletes_worksCorrectly() {
        val task1 = Task(title = "A")
        val task2 = Task(title = "B")
        val task3 = Task(title = "C")

        runBlocking {
            taskDao.insertTask(task1)
            taskDao.insertTask(task2)
            taskDao.insertTask(task3)
        }

        val all = runBlocking { taskDao.getAllTasks().first() }
        val toDelete = all.find { it.title == "B" }!!
        runBlocking { taskDao.deleteTask(toDelete) }

        val remaining = runBlocking { taskDao.getAllTasks().first() }
        Assert.assertEquals(2, remaining.size)
        Assert.assertTrue(remaining.any { it.title == "A" })
        Assert.assertTrue(remaining.any { it.title == "C" })
    }
}
