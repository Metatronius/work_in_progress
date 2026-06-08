/** Activity that presents a form for the user to enter a new task and return it to [MainActivity]. */
package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.util.DataUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen that collects task details (title, date, priority, notes, reminder) from the user
 * and returns them to the caller via [android.app.Activity.setResult].
 */
class AddTask : AppCompatActivity() {

    private var position: Int = -1

    private val dateFormat =
        SimpleDateFormat("MM/dd/yyyy", Locale.US)

     /**
     * Initializes the Add Task screen, wires up UI components, and returns the
     * collected task data to the caller via an [android.content.Intent] result
     * when the user taps Save.
     *
     * @param savedInstanceState Previously saved instance state, or null.
     */
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
            if (!date.isBlank()) {
                try {
                    DataUtil.validateDate(date)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(
                        this,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }
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
}
