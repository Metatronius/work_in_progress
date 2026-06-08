/** Activity that displays the full details of a single task in a read-only view. */
package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.entities.Task

/** Read-only detail screen that shows the title, due date, priority, and notes of a task. */
class TaskDetail : AppCompatActivity() {

    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val taskId = intent.getIntExtra("TASK_ID", -1)
        val title = intent.getStringExtra("TITLE")
        val date = intent.getStringExtra("DATE")
        val priority = intent.getStringExtra("PRIORITY")
        val notes = intent.getStringExtra("NOTES")
        val created = intent.getStringExtra("CREATED")
        val remind = intent.getBooleanExtra("REMIND", false)
        val progressValue = intent.getIntExtra("PROGRESS", 0)
        val targetValue = intent.getIntExtra("TARGET", 1)

        position = intent.getIntExtra("POSITION", -1)

        @Suppress("DEPRECATION")
        val taskList = intent.getParcelableArrayListExtra<Task>("TASK_LIST") ?: arrayListOf()

        val completedTasks = taskList.count { it.progress >= it.target }
        val pendingTasks = taskList.size - completedTasks

        val titleView = findViewById<TextView>(R.id.detailTitle)
        val dateView = findViewById<TextView>(R.id.detailDate)
        val priorityView = findViewById<TextView>(R.id.detailPriority)
        val notesView = findViewById<TextView>(R.id.detailNotes)

        val completedView = findViewById<TextView>(R.id.completedTasksText)
        val pendingView = findViewById<TextView>(R.id.pendingTasksText)
        val progressBar = findViewById<ProgressBar>(R.id.taskProgressBar)

        val editButton = findViewById<Button>(R.id.editButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)

        titleView.text = title
        dateView.text = "Due: $date"
        priorityView.text = "Priority: $priority"
        notesView.text = "Notes: $notes"

        completedView.text = "Completed Tasks: $completedTasks"
        pendingView.text = "Pending Tasks: $pendingTasks"

        progressBar.max = taskList.size
        progressBar.progress = completedTasks

        deleteButton.setOnClickListener {

            val resultIntent = Intent()
            resultIntent.putExtra("TASK_ID", taskId)

            setResult(3, resultIntent)

            finish()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditTask::class.java).apply {
                putExtra("TASK_ID", taskId)
                putExtra("TITLE", title)
                putExtra("DATE", date)
                putExtra("PRIORITY", priority)
                putExtra("NOTES", notes)
                putExtra("CREATED", created)
                putExtra("REMIND", remind)
                putExtra("PROGRESS", progressValue)
                putExtra("TARGET", targetValue)
            }

            @Suppress("DEPRECATION")
            startActivityForResult(intent, 2)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        if (
            requestCode == 2 &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Handles action bar item selections. Pressing the Up/Home button finishes
     * the activity and returns to the previous screen.
     *
     * @param item The menu item that was selected.
     * @return `true` if the event was handled; delegates to super otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
