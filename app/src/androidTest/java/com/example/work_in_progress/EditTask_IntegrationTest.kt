package com.example.work_in_progress

import android.app.Instrumentation
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
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
class EditTask_IntegrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(
        Intent(ApplicationProvider.getApplicationContext(), EditTask::class.java).apply {
            putExtra("TASK_ID", 1)
            putExtra("TITLE", "Original Title")
            putExtra("NOTES", "Original Notes")
            putExtra("PRIORITY", 1)
            putExtra("DATE", "05/19/2026")
            putExtra("REMIND", true)
            putExtra("PROGRESS", 0)
            putExtra("TARGET", 1)
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
            repository.insert(Task(id = 1, title = "Original Title", notes = "Original Notes", priority = 1, due = "05/19/2026", remind = true))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun editTask_formPreFilled_withIntentData() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val notesField = activity.findViewById<EditText>(R.id.notes_input)

            assert(titleField.text.toString() == "Original Title")
            assert(notesField.text.toString() == "Original Notes")
        }
    }

    @Test
    fun editTask_modifyTitle_persistsToDatabase() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("Updated Title")
            saveButton.performClick()
        }

        runBlocking {
            Thread.sleep(500)
            val task = repository.getTaskById(1)
            assert(task?.title == "Updated Title")
        }
    }

    @Test
    fun editTask_modifyNotes_persistsToDatabase() {
        activityRule.scenario.onActivity { activity ->
            val notesField = activity.findViewById<EditText>(R.id.notes_input)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            notesField.setText("Updated Notes")
            saveButton.performClick()
        }

        runBlocking {
            Thread.sleep(500)
            val task = repository.getTaskById(1)
            assert(task?.notes == "Updated Notes")
        }
    }

    @Test
    fun editTask_blankTitle_rejectsUpdate() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("")
            saveButton.performClick()

            Thread.sleep(300)
            assert(activity.isVisible || activity.hasWindowFocus())
        }
    }

    @Test
    fun editTask_cancelButton_closesWithoutUpdate() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val cancelButton = activity.findViewById<Button>(R.id.cancel_button)

            titleField.setText("Should Not Save")
            cancelButton.performClick()
        }

        runBlocking {
            Thread.sleep(300)
            val task = repository.getTaskById(1)
            assert(task?.title == "Original Title")
        }
    }
}
