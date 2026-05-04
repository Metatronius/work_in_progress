package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.database.Task
import com.example.work_in_progress.database.TaskParams
import com.example.work_in_progress.extensions.getTaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private val viewModel by lazy { getTaskViewModel() }
    private lateinit var searchBar: EditText

    /** In-memory cache of the latest task list from the database, used for search filtering. */
    private var currentTasks: List<Task> = emptyList()

    companion object {
        /** Request code used when launching [AddTask] for a result. */
        private const val REQUEST_ADD_TASK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.allTasks.observe(this) { tasks ->
            renderTasks(tasks)
        }

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        taskContainer = findViewById(R.id.taskContainer)
        searchBar = findViewById(R.id.searchBar)

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_ADD_TASK)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { displayTasks(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    @Deprecated("Use ActivityResultLauncher instead.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_TASK && resultCode == Activity.RESULT_OK) {
            val priorityValue = when (data?.getStringExtra("PRIORITY")) {
                "Low"    -> 1
                "Medium" -> 2
                "High"   -> 3
                else     -> 0
            }
            val params = TaskParams(
                title    = data?.getStringExtra("TITLE") ?: "",
                notes    = data?.getStringExtra("NOTES") ?: "",
                priority = priorityValue,
                due      = data?.getStringExtra("DATE")?.takeIf { it.isNotBlank() },
                remind   = data?.getBooleanExtra("REMINDER", false) ?: false
            )
            viewModel.addTask(params)
        }
    }

    private fun displayTasks(query: String) {
        taskContainer.removeAllViews()

        for (task in currentTasks) {
            if (!task.title.contains(query, ignoreCase = true)) continue

            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val checkBox = CheckBox(this).apply {
                isChecked = task.progress > 0
                setOnCheckedChangeListener { _, _ -> viewModel.completeTask(task) }
            }

            val titleView = TextView(this).apply {
                text = task.title
                textSize = 18f
                setPadding(8, 0, 0, 0)
                setOnClickListener {
                    val priorityLabel = when (task.priority) {
                        1 -> "Low"; 2 -> "Medium"; 3 -> "High"; else -> "None"
                    }
                    val intent = Intent(this@MainActivity, TaskDetail::class.java).apply {
                        putExtra("TITLE",    task.title)
                        putExtra("DATE",     task.due ?: "")
                        putExtra("PRIORITY", priorityLabel)
                        putExtra("NOTES",    task.notes)
                    }
                    startActivity(intent)
                }
                setOnLongClickListener {
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete \"${task.title}\"?")
                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteTask(task) }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }

            rowLayout.addView(checkBox)
            rowLayout.addView(titleView)
            taskContainer.addView(rowLayout)
        }
    }

    private fun renderTasks(tasks: List<Task>) {
        currentTasks = tasks
        displayTasks(searchBar.text.toString())
    }
}