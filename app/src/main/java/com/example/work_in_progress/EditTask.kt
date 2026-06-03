package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.extensions.getTaskViewModel
import com.example.work_in_progress.util.DataUtil

/**
 * Activity for editing an existing task.
 *
 * This activity retrieves task details from the intent and initializes the user interface
 * for editing the task's title, date, priority, notes, and reminder settings.
 *
 * @param savedInstanceState A Bundle containing the activity's previously saved state.
 *                           If the activity has never existed before, this value is null.
 */
class EditTask : AppCompatActivity() {

    /**
     * Called when the activity is starting. This is where you initialize your activity.
     * It sets the content view to the layout for editing a task and retrieves task details
     * from the intent extras.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // TODO: BUG - Line 35: viewModel is declared by lazy but never used. Activity just returns Intent result, doesn't use ViewModel. Remove if not needed.
        val viewModel by lazy { getTaskViewModel() }

        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TITLE") ?: ""
        val taskDate = intent.getStringExtra("DATE") ?: ""
        val taskPriority = intent.getStringExtra("PRIORITY") ?: ""
        val taskNotes = intent.getStringExtra("NOTES") ?: ""
        val taskRemind = intent.getBooleanExtra("REMIND", false)
        val taskProgress = intent.getIntExtra("PROGRESS", 0)
        val taskTarget = intent.getIntExtra("TARGET", 1)

        val titleField = findViewById<EditText>(R.id.editTaskTitle)
        val dateField = findViewById<EditText>(R.id.editTaskDate)
        val notesField = findViewById<EditText>(R.id.editTaskNotes)
        val reminderSwitch = findViewById<Switch>(R.id.editReminderSwitch)
        val priorityGroup = findViewById<RadioGroup>(R.id.editPriorityGroup)
        val saveButton = findViewById<Button>(R.id.editSaveButton)

        // Pre-fill fields with existing task data
        titleField.setText(taskTitle)
        dateField.setText(taskDate)
        notesField.setText(taskNotes)
        reminderSwitch.isChecked = taskRemind

        when (taskPriority) {
            "High" -> priorityGroup.check(R.id.editHighPriority)
            "Medium" -> priorityGroup.check(R.id.editMediumPriority)
            "Low" -> priorityGroup.check(R.id.editLowPriority)
            // TODO: BUG - Line 63: Missing "None" priority case. If taskPriority is "None", no radio button gets checked.
        }

        saveButton.setOnClickListener {
            val selectedPriorityId = priorityGroup.checkedRadioButtonId
            val priorityLabel = if (selectedPriorityId != -1) {
                findViewById<RadioButton>(selectedPriorityId).text.toString()
            } else "None"

            val priorityValue = DataUtil.getPriorityValue(priorityLabel)

            val resultIntent = Intent().apply {
                putExtra("TASK_ID", taskId)
                putExtra("TITLE", titleField.text.toString())
                putExtra("DATE", dateField.text.toString())
                putExtra("PRIORITY", priorityValue)
                putExtra("NOTES", notesField.text.toString())
                putExtra("REMIND", reminderSwitch.isChecked)
                putExtra("PROGRESS", taskProgress)
                putExtra("TARGET", taskTarget)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
