package com.example.work_in_progress

import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
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
class TaskDetail_IntegrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(
        Intent(ApplicationProvider.getApplicationContext(), TaskDetail::class.java).apply {
            putExtra("TITLE", "Test Task")
            putExtra("NOTES", "Test Notes")
            putExtra("PRIORITY", "Medium")
            putExtra("DATE", "05/20/2026")
        }
    )

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext()
        db = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = TaskRepository(db.taskDao())

        runBlocking {
            repository.insert(Task(title = "Test Task", notes = "Test Notes", priority = 2, due = "05/20/2026"))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun taskDetail_displaysTitle_fromIntentExtra() {
        activityRule.scenario.onActivity { activity ->
            val titleView = activity.findViewById<TextView>(R.id.task_detail_title)
            assert(titleView.text.toString() == "Test Task")
        }
    }

    @Test
    fun taskDetail_displaysNotes_fromIntentExtra() {
        activityRule.scenario.onActivity { activity ->
            val notesView = activity.findViewById<TextView>(R.id.task_detail_notes)
            assert(notesView.text.toString() == "Test Notes")
        }
    }

    @Test
    fun taskDetail_displaysPriority_fromIntentExtra() {
        activityRule.scenario.onActivity { activity ->
            val priorityView = activity.findViewById<TextView>(R.id.task_detail_priority)
            assert(priorityView.text.toString().contains("Medium"))
        }
    }

    @Test
    fun taskDetail_displaysDueDate_fromIntentExtra() {
        activityRule.scenario.onActivity { activity ->
            val dateView = activity.findViewById<TextView>(R.id.task_detail_date)
            assert(dateView.text.toString() == "05/20/2026")
        }
    }

    @Test
    fun taskDetail_nullNotes_displaysEmptyOrPlaceholder() {
        val nullNotesIntent = Intent(ApplicationProvider.getApplicationContext(), TaskDetail::class.java).apply {
            putExtra("TITLE", "No Notes Task")
            putExtra("PRIORITY", "None")
            // Omit NOTES
        }

        ActivityScenarioRule(nullNotesIntent).use { rule ->
            rule.scenario.onActivity { activity ->
                val notesView = activity.findViewById<TextView>(R.id.task_detail_notes)
                assert(notesView.text.toString().isEmpty() || notesView.text.toString() == "-")
            }
        }
    }
}
