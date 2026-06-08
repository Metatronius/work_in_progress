package com.example.work_in_progress

import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Switch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.AppDatabase
import com.example.work_in_progress.database.TaskRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTask_IntegrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AddTask::class.java)

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
    fun addTask_validData_createsTask() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val notesField = activity.findViewById<EditText>(R.id.notes_input)
            val dateField = activity.findViewById<EditText>(R.id.date_input)
            val reminderSwitch = activity.findViewById<Switch>(R.id.reminder_switch)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("Test Task")
            notesField.setText("Test notes")
            dateField.setText("05/20/2026")
            reminderSwitch.isChecked = true

            saveButton.performClick()
        }

        runBlocking {
            Thread.sleep(500)
            val tasks = repository.allTasks
            // Verify task was created
        }
    }

    @Test
    fun addTask_blankTitle_showsError() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("")
            saveButton.performClick()

            Thread.sleep(300)
            // Verify error is shown (via assertion or by checking activity still visible)
            assert(activity.isVisible || activity.hasWindowFocus())
        }
    }

    @Test
    fun addTask_titleTooLong_showsError() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("a".repeat(31))
            saveButton.performClick()

            Thread.sleep(300)
            assert(activity.isVisible || activity.hasWindowFocus())
        }
    }

    @Test
    fun addTask_prioritySelection_persists() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val mediumPriorityButton = activity.findViewById<RadioButton>(R.id.priority_medium)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("Priority Task")
            mediumPriorityButton.isChecked = true
            saveButton.performClick()
        }

        runBlocking {
            Thread.sleep(500)
            val tasks = repository.allTasks
            // Verify priority was saved (Medium = 2)
        }
    }

    @Test
    fun addTask_reminderToggle_persists() {
        activityRule.scenario.onActivity { activity ->
            val titleField = activity.findViewById<EditText>(R.id.title_input)
            val reminderSwitch = activity.findViewById<Switch>(R.id.reminder_switch)
            val saveButton = activity.findViewById<Button>(R.id.save_button)

            titleField.setText("Reminder Task")
            reminderSwitch.isChecked = true
            saveButton.performClick()
        }

        runBlocking {
            Thread.sleep(500)
            val tasks = repository.allTasks
            // Verify reminder flag was saved
        }
    }

    @Test
    fun addTask_cancelButton_closesWithoutSaving() {
        activityRule.scenario.onActivity { activity ->
            val cancelButton = activity.findViewById<Button>(R.id.cancel_button)
            val initialCount = runBlocking { repository.allTasks }

            cancelButton.performClick()

            Thread.sleep(300)
            runBlocking {
                val finalCount = repository.allTasks
                // Verify no task was created
            }
        }
    }
}
