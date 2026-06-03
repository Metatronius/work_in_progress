package com.example.work_in_progress

import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.AppDatabase
import com.example.work_in_progress.database.TaskRepository
import com.example.work_in_progress.entities.Task
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskFlow_E2ETest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext()
        db = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = TaskRepository(db.taskDao())
        Intents.init()
    }

    @After
    fun tearDown() {
        db.close()
        Intents.release()
    }

    @Test
    fun e2e_createTask_appearsInList() {
        activityRule.scenario.onActivity { activity ->
            val initialCount = activity.currentTasks.size
            assert(initialCount == 0)
        }

        // Simulate creating a task via AddTask activity
        runBlocking {
            repository.insert(Task(title = "E2E Test Task", priority = 1))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(500)
            val updatedCount = activity.currentTasks.size
            assert(updatedCount == 1)
            assert(activity.currentTasks[0].title == "E2E Test Task")
        }
    }

    @Test
    fun e2e_createMultipleTasks_allAppearInList() {
        runBlocking {
            repository.insert(Task(title = "First Task", priority = 0))
            repository.insert(Task(title = "Second Task", priority = 1))
            repository.insert(Task(title = "Third Task", priority = 2))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(500)
            val tasks = activity.currentTasks
            assert(tasks.size == 3)
            assert(tasks[0].title == "First Task")
            assert(tasks[1].title == "Second Task")
            assert(tasks[2].title == "Third Task")
        }
    }

    @Test
    fun e2e_markTaskComplete_updatesDatabase() {
        runBlocking {
            repository.insert(Task(id = 1, title = "Complete This", priority = 0, progress = 0))
        }

        activityRule.scenario.onActivity { activity ->
            val task = activity.currentTasks[0]
            assert(task.progress == 0)
        }

        // Toggle completion
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let {
                val toggled = it.copy(progress = (it.progress + 1) % 2)
                repository.update(toggled)
            }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            val task = activity.currentTasks[0]
            assert(task.progress == 1)
        }
    }

    @Test
    fun e2e_deleteTask_removesFromList() {
        runBlocking {
            val id = repository.insert(Task(title = "Delete Me", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks.size == 1)
        }

        // Delete the task
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let { repository.delete(it) }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks.size == 0)
        }
    }

    @Test
    fun e2e_editTask_updatesInList() {
        runBlocking {
            repository.insert(Task(id = 1, title = "Original", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks[0].title == "Original")
        }

        // Edit the task
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let {
                val updated = it.copy(title = "Updated", priority = 2)
                repository.update(updated)
            }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            val task = activity.currentTasks[0]
            assert(task.title == "Updated")
            assert(task.priority == 2)
        }
    }

    @Test
    fun e2e_searchFilter_showsOnlyMatches() {
        runBlocking {
            repository.insert(Task(title = "Buy Milk", priority = 0))
            repository.insert(Task(title = "Fix Bug", priority = 1))
            repository.insert(Task(title = "Call Mom", priority = 2))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks.size == 3)

            val searchField = activity.findViewById<android.widget.EditText>(R.id.search_input)
            searchField.setText("Buy")
            Thread.sleep(300)

            val filtered = activity.currentTasks
            assert(filtered.size == 1)
            assert(filtered[0].title == "Buy Milk")
        }
    }

    @Test
    fun e2e_complexFlow_createEditDelete() {
        // Create
        runBlocking {
            repository.insert(Task(id = 1, title = "Complex Task", priority = 1, notes = "Original notes"))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks.size == 1)
        }

        // Edit
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let {
                val updated = it.copy(title = "Updated Task", notes = "Updated notes", priority = 3)
                repository.update(updated)
            }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            val task = activity.currentTasks[0]
            assert(task.title == "Updated Task")
            assert(task.notes == "Updated notes")
            assert(task.priority == 3)
        }

        // Mark complete
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let {
                val completed = it.copy(progress = 1)
                repository.update(completed)
            }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            val task = activity.currentTasks[0]
            assert(task.progress == 1)
        }

        // Delete
        runBlocking {
            val task = repository.getTaskById(1)
            task?.let { repository.delete(it) }
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            assert(activity.currentTasks.size == 0)
        }
    }

    @Test
    fun e2e_taskOrdering_mostRecentFirst() {
        runBlocking {
            repository.insert(Task(id = 1, title = "First", priority = 0))
            Thread.sleep(10)
            repository.insert(Task(id = 2, title = "Second", priority = 0))
            Thread.sleep(10)
            repository.insert(Task(id = 3, title = "Third", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            Thread.sleep(300)
            val tasks = activity.currentTasks
            // Tasks should be ordered by ID descending (most recent first)
            assert(tasks[0].title == "Third")
            assert(tasks[1].title == "Second")
            assert(tasks[2].title == "First")
        }
    }
}
