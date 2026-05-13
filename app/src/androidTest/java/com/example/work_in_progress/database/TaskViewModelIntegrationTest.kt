package com.example.work_in_progress.database

import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.util.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [TaskViewModel] with a real Room database.
 * Tests verify end-to-end workflows including validation and database persistence.
 */
@RunWith(AndroidJUnit4::class)
class TaskViewModelIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        repository = TaskRepository(database.taskDao())
        viewModel = TaskViewModel(repository)
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun addTask_persistsToDatabase() {
        val params = TaskParams(title = "Test Task")
        viewModel.addTask(params)

        Thread.sleep(1000)
        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)
        Assert.assertEquals("Test Task", tasks[0].title)
    }

    @Test
    fun addTask_withAllFields_persistsAllData() {
        val params = TaskParams(
            title = "Complete Task",
            notes = "Task notes",
            priority = Priority.HIGH,
            due = "5/20/2025",
            remind = true,
            progress = 0
        )
        viewModel.addTask(params)

        Thread.sleep(1000)
        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)
        val task = tasks[0]
        Assert.assertEquals("Complete Task", task.title)
        Assert.assertEquals("Task notes", task.notes)
        Assert.assertEquals(3, task.priority)
        Assert.assertEquals("5/20/2025", task.due)
        Assert.assertTrue(task.remind)
        Assert.assertEquals(0, task.progress)
        Assert.assertEquals(1, task.target)
    }

    @Test
    fun addTask_titleValidation_rejectsBlank() {
        val params = TaskParams(title = "")
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
    }

    @Test
    fun addTask_titleValidation_rejectsOver30Chars() {
        val params = TaskParams(title = "a".repeat(31))
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
    }

    @Test
    fun addTask_accepts30CharTitle() {
        val params = TaskParams(title = "a".repeat(30))
        viewModel.addTask(params)

        Thread.sleep(1000)
        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)
        Assert.assertEquals("a".repeat(30), tasks[0].title)
    }

    @Test
    fun deleteTask_removesFromDatabase() {
        val task = Task(title = "To Delete")
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)

        viewModel.deleteTask(tasks[0])
        Thread.sleep(1000)

        val remaining = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(0, remaining.size)
    }

    @Test
    fun completeTask_togglesProgress() {
        val task = Task(title = "Incomplete", progress = 0)
        runBlocking { repository.insert(task) }

        val tasks = runBlocking { repository.allTasks.first() }
        viewModel.completeTask(tasks[0])
        Thread.sleep(1000)

        val updated = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, updated[0].progress)
    }

    @Test
    fun addTask_dateValidation_rejectsInvalidFormat() {
        val params = TaskParams(title = "Test", due = "invalid-date")
        Assert.assertThrows(Exception::class.java) {
            viewModel.addTask(params)
        }
    }

    @Test
    fun addTask_dateValidation_acceptsValidFormat() {
        val params = TaskParams(title = "Test", due = "5/15/2025")
        viewModel.addTask(params)

        Thread.sleep(1000)
        val tasks = runBlocking { repository.allTasks.first() }
        Assert.assertEquals(1, tasks.size)
        Assert.assertEquals("5/15/2025", tasks[0].due)
    }

    @Test
    fun allTasks_liveDataNotifiesObserver() {
        val observedTasks = mutableListOf<List<Task>>()
        val observer = Observer<List<Task>> { observedTasks.add(it) }

        viewModel.allTasks.observeForever(observer)
        Thread.sleep(500)

        val params = TaskParams(title = "New Task")
        viewModel.addTask(params)
        Thread.sleep(1000)

        viewModel.allTasks.removeObserver(observer)
        Assert.assertTrue(observedTasks.size > 0)
    }
}
