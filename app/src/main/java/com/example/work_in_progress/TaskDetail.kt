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

class TaskDetail : AppCompatActivity() {

    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("TITLE")
        val date = intent.getStringExtra("DATE")
        val priority = intent.getStringExtra("PRIORITY")
        val notes = intent.getStringExtra("NOTES")

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
            resultIntent.putExtra("POSITION", position)

            setResult(3, resultIntent)

            finish()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)

            intent.putExtra("TITLE", title)
            intent.putExtra("DATE", date)
            intent.putExtra("PRIORITY", priority)
            intent.putExtra("NOTES", notes)
            intent.putExtra("POSITION", position)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
