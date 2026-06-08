/** Main entry-point Activity that lists tasks, supports search filtering, and launches [AddTask]. */
package com.example.work_in_progress

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.work_in_progress.dtos.TaskParams
import com.example.work_in_progress.entities.Task
import com.example.work_in_progress.extensions.getTaskViewModel
import com.example.work_in_progress.util.DataUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main screen that displays all tasks in a scrollable list, provides a search bar for
 * filtering by title, and navigates to [AddTask] to create new tasks.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var taskContainer: LinearLayout
    private val viewModel by lazy { getTaskViewModel() }
    private lateinit var searchBar: EditText
    private lateinit var dueSoonPlaceholder: TextView
    private lateinit var bottomNav: BottomNavigationView

    private var currentTasks: List<Task> = emptyList()

    companion object {
        /** Request code used when launching [AddTask] for a result. */
        private const val REQUEST_ADD_TASK = 1
        /** Request code used when launching [EditTask] for a result. */
        private const val REQUEST_EDIT_TASK = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        taskContainer = findViewById(R.id.taskContainer)
        searchBar = findViewById(R.id.searchBar)
        dueSoonPlaceholder = findViewById(R.id.dueSoonPlaceholder)
        bottomNav = findViewById(R.id.bottomNavigation)

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)

        viewModel.allTasks.observe(this) { tasks ->
            renderTasks(tasks)
        }

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_ADD_TASK)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                displayTasks(s.toString())
            }
        })

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    intent.putParcelableArrayListExtra("TASK_LIST", ArrayList(currentTasks.filter { it.due != null }))
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_home
    }

    @SuppressLint("SetTextI18n")
    private fun updateDueSoon() {
        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val today = Calendar.getInstance().time
        val dueSoonTasks = ArrayList<String>()

        for (task in currentTasks) {
            try {
                val due = task.due ?: continue
                val taskDate = formatter.parse(due)
                if (taskDate != null) {
                    val diff = taskDate.time - today.time
                    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                    if (days in 0..7) {
                        val timeText = when (days) {
                            0 -> "Today"
                            1 -> "Tomorrow"
                            else -> "$days days"
                        }
                        dueSoonTasks.add("• ${task.title} ($timeText)")
                    }
                }
            } catch (e: Exception) {}
        }

        dueSoonPlaceholder.text = if (dueSoonTasks.isEmpty()) {
            "No tasks due soon"
        } else {
            dueSoonTasks.joinToString("\n")
        }
    }

    @Deprecated("Use ActivityResultLauncher instead.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_TASK && resultCode == Activity.RESULT_OK) {
            val priorityValue = DataUtil.getPriority(data?.getStringExtra("PRIORITY") ?: "None")
            val params = TaskParams(
                title = data?.getStringExtra("TITLE") ?: "",
                notes = data?.getStringExtra("NOTES") ?: "",
                priority = priorityValue,
                due = data?.getStringExtra("DATE")?.takeIf { it.isNotBlank() },
                remind = data?.getBooleanExtra("REMINDER", false) ?: false
            )
            viewModel.addTask(params) { taskId ->
                if (params.remind && !params.due.isNullOrBlank()) {
                    ReminderScheduler.schedule(this, taskId, params.title, params.due)
                }
            }
        }

        if (requestCode == REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK) {
            val id = data?.getIntExtra("TASK_ID", -1) ?: -1
            val title = data?.getStringExtra("TITLE") ?: ""
            val notes = data?.getStringExtra("NOTES") ?: ""
            val priority = data?.getIntExtra("PRIORITY", 0) ?: 0
            val created = data?.getStringExtra("CREATED") ?: ""
            val due = data?.getStringExtra("DATE")
            val remind = data?.getBooleanExtra("REMIND", false) ?: false
            val progress = data?.getIntExtra("PROGRESS", 0) ?: 0
            val target = data?.getIntExtra("TARGET", 1) ?: 1

            if (id != -1) {
                viewModel.editTask(Task(id, title, notes, priority, created, due, remind, progress, target))
                ReminderScheduler.cancel(this, id)
                if (remind && !due.isNullOrBlank()) {
                    ReminderScheduler.schedule(this, id, title, due)
                }
            }
        }

        if (requestCode == REQUEST_EDIT_TASK && resultCode == 3) {
            val id = data?.getIntExtra("TASK_ID", -1) ?: -1
            if (id != -1) {
                ReminderScheduler.cancel(this, id)
                viewModel.deleteTaskById(id)
            }
        }
    }

    private fun displayTasks(query: String) {
        taskContainer.removeAllViews()

        // Sort tasks: Incomplete first, then completed.
        val sortedTasks = currentTasks.sortedBy { it.progress >= it.target }

        for (task in sortedTasks) {
            if (query.isNotEmpty() && !task.title.contains(query, ignoreCase = true)) continue

            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val isCompleted = task.progress >= task.target

            val checkBox = CheckBox(this).apply {
                isChecked = isCompleted
                setOnCheckedChangeListener { _, _ -> viewModel.completeTask(task) }
            }

            val titleView = TextView(this).apply {
                text = task.title
                textSize = 18f
                setPadding(8, 0, 0, 0)

                // Add strikethrough and dim color if completed
                if (isCompleted) {
                    paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(android.graphics.Color.GRAY)
                }

                setOnClickListener {
                    val priorityLabel = DataUtil.getPriorityName(task.priority)
                    val intent = Intent(this@MainActivity, TaskDetail::class.java).apply {
                        putExtra("TASK_ID",  task.id)
                        putExtra("TITLE",    task.title)
                        putExtra("DATE",     task.due ?: "")
                        putExtra("PRIORITY", priorityLabel)
                        putExtra("NOTES",    task.notes)
                        putExtra("CREATED",  task.created)
                        putExtra("REMIND",   task.remind)
                        putExtra("PROGRESS", task.progress)
                        putExtra("TARGET",   task.target)
                        putParcelableArrayListExtra("TASK_LIST", ArrayList(currentTasks))
                    }
                    @Suppress("DEPRECATION")
                    startActivityForResult(intent, REQUEST_EDIT_TASK)
                }
                setOnLongClickListener {
                    val options = arrayOf("Edit", "Delete")
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle(task.title)
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val priorityLabel = DataUtil.getPriorityName(task.priority)
                                    val intent = Intent(this@MainActivity, EditTask::class.java).apply {
                                        putExtra("TASK_ID",  task.id)
                                        putExtra("TITLE",    task.title)
                                        putExtra("DATE",     task.due ?: "")
                                        putExtra("PRIORITY", priorityLabel)
                                        putExtra("NOTES",    task.notes)
                                        putExtra("CREATED",  task.created)
                                        putExtra("REMIND",   task.remind)
                                        putExtra("PROGRESS", task.progress)
                                        putExtra("TARGET",   task.target)
                                    }
                                    @Suppress("DEPRECATION")
                                    startActivityForResult(intent, REQUEST_EDIT_TASK)
                                }
                                1 -> {
                                    android.app.AlertDialog.Builder(this@MainActivity)
                                        .setTitle("Delete Task")
                                        .setMessage("Are you sure you want to delete \"${task.title}\"?")
                                        .setPositiveButton("Delete") { _, _ ->
                                            ReminderScheduler.cancel(this@MainActivity, task.id)
                                            viewModel.deleteTask(task)
                                        }
                                        .setNegativeButton("Cancel", null)
                                        .show()
                                }
                            }
                        }
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
        updateDueSoon()
        displayTasks(searchBar.text.toString())
    }
}
