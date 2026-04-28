package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.database.Task
import com.example.work_in_progress.extensions.getTaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private val viewModel by lazy { getTaskViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.allTasks.observe(this) {tasks ->
            renderTasks(tasks)
        }

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        taskContainer = findViewById(R.id.taskContainer)

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val title = data?.getStringExtra("TASK_TITLE")

            if (title != null && title.isNotEmpty()) {
                val newTask = TextView(this)
                newTask.text = title
                newTask.textSize = 18f

                taskContainer.addView(newTask)
            }
        }
    }

    private fun renderTasks(tasks: List<Task>) {
        // Just print the tasks for now
        tasks.forEach { task ->
            print(task.title)
        }
    }
}