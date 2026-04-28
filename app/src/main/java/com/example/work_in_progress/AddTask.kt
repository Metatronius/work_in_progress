package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddTask : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val taskTitle = findViewById<EditText>(R.id.taskTitle)
        val taskDate = findViewById<EditText>(R.id.taskDate)
        val taskNotes = findViewById<EditText>(R.id.taskNotes)
        val reminderSwitch = findViewById<Switch>(R.id.reminderSwitch)
        val priorityGroup = findViewById<RadioGroup>(R.id.priorityGroup)


        saveButton.setOnClickListener {

            val title = taskTitle.text.toString()
            val date = taskDate.text.toString()
            val notes = taskNotes.text.toString()
            val reminder = reminderSwitch.isChecked

            val selectedPriorityId = priorityGroup.checkedRadioButtonId
            val priority = if (selectedPriorityId != -1) {
                findViewById<RadioButton>(selectedPriorityId).text.toString()
            } else {
                "None"
            }

            val resultIntent = Intent()
            resultIntent.putExtra("TITLE", title)
            resultIntent.putExtra("DATE", date)
            resultIntent.putExtra("PRIORITY", priority)
            resultIntent.putExtra("NOTES", notes)
            resultIntent.putExtra("REMINDER", reminder)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
