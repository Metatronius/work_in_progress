package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddTask : AppCompatActivity() {

    private var position: Int = -1

    private val dateFormat =
        SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val taskTitle =
            findViewById<EditText>(R.id.taskTitle)

        val taskDate =
            findViewById<EditText>(R.id.taskDate)

        val taskNotes =
            findViewById<EditText>(R.id.taskNotes)

        val reminderSwitch =
            findViewById<Switch>(R.id.reminderSwitch)

        val priorityGroup =
            findViewById<RadioGroup>(R.id.priorityGroup)

        position = intent.getIntExtra("POSITION", -1)

        taskTitle.setText(
            intent.getStringExtra("TITLE") ?: ""
        )

        taskDate.setText(
            intent.getStringExtra("DATE") ?: ""
        )

        taskNotes.setText(
            intent.getStringExtra("NOTES") ?: ""
        )

        saveButton.setOnClickListener {

            val title =
                taskTitle.text.toString().trim()

            val date =
                taskDate.text.toString().trim()

            val notes =
                taskNotes.text.toString().trim()

            val reminder =
                reminderSwitch.isChecked

            // add title validation
            if (title.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter a task title",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // date validation
            if (!isValidDate(date)) {

                Toast.makeText(
                    this,
                    "Invalid date. Use MM/DD/YYYY",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val selectedPriorityId =
                priorityGroup.checkedRadioButtonId

            val priority =
                if (selectedPriorityId != -1) {

                    findViewById<RadioButton>(
                        selectedPriorityId
                    ).text.toString()

                } else {
                    "None"
                }

            val resultIntent = Intent()

            resultIntent.putExtra("TITLE", title)
            resultIntent.putExtra("DATE", date)
            resultIntent.putExtra("PRIORITY", priority)
            resultIntent.putExtra("NOTES", notes)
            resultIntent.putExtra("REMINDER", reminder)
            resultIntent.putExtra("POSITION", position)

            setResult(Activity.RESULT_OK, resultIntent)

            finish()
        }
    }

    private fun isValidDate(date: String): Boolean {

        return try {

            dateFormat.isLenient = false

            val parsedDate = dateFormat.parse(date)

            parsedDate != null

        } catch (e: Exception) {

            false
        }
    }
}