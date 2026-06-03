package com.example.work_in_progress

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.AppDatabase
import com.example.work_in_progress.database.TaskRepository
import com.example.work_in_progress.dtos.TaskParams
import com.example.work_in_progress.entities.Task
import com.example.work_in_progress.util.Priority
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivity_SearchTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext()
        db = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = TaskRepository(db.taskDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun search_filtersByTitle_caseInsensitive() {
        runBlocking {
            repository.insert(Task(title = "Buy Groceries", priority = 1))
            repository.insert(Task(title = "Call Mom", priority = 2))
            repository.insert(Task(title = "Fix Bug", priority = 3))
        }

        activityRule.scenario.onActivity { activity ->
            val searchField = activity.findViewById<EditText>(R.id.search_input)
            searchField.setText("buy")
            Thread.sleep(500)

            val tasks = activity.currentTasks
            assert(tasks.size == 1)
            assert(tasks[0].title == "Buy Groceries")
        }
    }

    @Test
    fun search_filtersMultipleTasks_partialMatch() {
        runBlocking {
            repository.insert(Task(title = "Task One", priority = 0))
            repository.insert(Task(title = "Task Two", priority = 0))
            repository.insert(Task(title = "Other", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            val searchField = activity.findViewById<EditText>(R.id.search_input)
            searchField.setText("Task")
            Thread.sleep(500)

            val tasks = activity.currentTasks
            assert(tasks.size == 2)
        }
    }

    @Test
    fun search_emptyFilter_showsAllTasks() {
        runBlocking {
            repository.insert(Task(title = "Task A", priority = 0))
            repository.insert(Task(title = "Task B", priority = 0))
            repository.insert(Task(title = "Task C", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            val searchField = activity.findViewById<EditText>(R.id.search_input)
            searchField.setText("")
            Thread.sleep(500)

            val tasks = activity.currentTasks
            assert(tasks.size == 3)
        }
    }

    @Test
    fun search_noMatches_returnsEmpty() {
        runBlocking {
            repository.insert(Task(title = "Buy Milk", priority = 0))
            repository.insert(Task(title = "Fix Roof", priority = 0))
        }

        activityRule.scenario.onActivity { activity ->
            val searchField = activity.findViewById<EditText>(R.id.search_input)
            searchField.setText("xyz")
            Thread.sleep(500)

            val tasks = activity.currentTasks
            assert(tasks.isEmpty())
        }
    }
}
