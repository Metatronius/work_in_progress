/** Main entry-point Activity that lists tasks, supports search filtering, and launches [AddTask]. */
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

/**
 * Main screen that displays all tasks in a scrollable list, provides a search bar for
 * filtering by title, and navigates to [AddTask] to create new tasks.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private val viewModel by lazy { getTaskViewModel() }
    private lateinit var searchBar: EditText

    /** In-memory cache of the latest task list from the database, used for search filtering. */
    private var currentTasks: List<Task> = emptyList()

    companion object {
        /** Request code used when launching [AddTask] for a result. */
        private const val REQUEST_ADD_TASK = 1
        /** Request code used when launching [EditTask] for a result. */
        private const val REQUEST_EDIT_TASK = 2
    }

    /**
     * Inflates the layout, binds UI views, observes the task list [LiveData], and wires up
     * the add-task button and search bar listeners.
     *
     * @param savedInstanceState Previously saved instance state, or null.
     */
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
            /**
             * Called to notify that the text has been changed.
             *
             * This method is invoked after the text in the input field has been modified.
             * It triggers the display of tasks based on the updated text.
             *
             * @param s The new text as an Editable, or null if no text is present.
             */
            override fun afterTextChanged(s: Editable?) { displayTasks(s.toString()) }
            /**
             * Called to notify that the text has been changed.
             *
             * @param s The new text as a CharSequence.
             * @param start The offset into the text where the change begins.
             * @param before The number of characters that were replaced.
             * @param count The number of characters that were added.
             */
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            /**
             * Called when an activity you launched exits, giving you the requestCode you started it with,
             * the resultCode it returned, and any additional data from it.
             *
             * @param requestCode The request code passed to startActivityForResult.
             * @param resultCode  The result code returned by the child activity.
             * @param data        The Intent carrying the task field extras, or null.
             *
             * @throws IllegalStateException if the requestCode is invalid.
             *
             * @deprecated Use ActivityResultLauncher instead.
             */
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Receives the result from [AddTask] and persists the new task via the ViewModel.
     *
     * @param requestCode The request code passed to startActivityForResult.
     * @param resultCode  The result code returned by the child activity.
     * @param data        The Intent carrying the task field extras, or null.
     */
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

        if (requestCode == REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK) {
            val id       = data?.getIntExtra("TASK_ID", -1) ?: -1
            val title    = data?.getStringExtra("TITLE") ?: ""
            val notes    = data?.getStringExtra("NOTES") ?: ""
            val priority = data?.getIntExtra("PRIORITY", 0) ?: 0
            val due      = data?.getStringExtra("DATE")
            val remind   = data?.getBooleanExtra("REMIND", false) ?: false
            val progress = data?.getIntExtra("PROGRESS", 0) ?: 0
            val target   = data?.getIntExtra("TARGET", 1) ?: 1

            if (id != -1) {
                viewModel.editTask(id, title, notes, priority, due, remind, progress, target)
            }
        }
    }

    /**
     * Clears [taskContainer] and re-renders only those tasks whose title contains [query]
     * (case-insensitive). Each row includes a completion checkbox and a tappable title.
     *
     * @param query The search string to filter tasks by title.
     */
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
                    val options = arrayOf("Edit", "Delete")
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle(task.title)
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    // Launch EditTask screen with existing task data
                                    val priorityLabel = when (task.priority) {
                                        1 -> "Low"; 2 -> "Medium"; 3 -> "High"; else -> "None"
                                    }
                                    val intent = Intent(this@MainActivity, EditTask::class.java).apply {
                                        putExtra("TASK_ID",  task.id)
                                        putExtra("TITLE",    task.title)
                                        putExtra("DATE",     task.due ?: "")
                                        putExtra("PRIORITY", priorityLabel)
                                        putExtra("NOTES",    task.notes)
                                        putExtra("REMIND",   task.remind)
                                        putExtra("PROGRESS", task.progress)
                                        putExtra("TARGET",   task.target)
                                    }
                                    @Suppress("DEPRECATION")
                                    startActivityForResult(intent, REQUEST_EDIT_TASK)
                                }
                                1 -> {
                                    // Confirm before deleting
                                    android.app.AlertDialog.Builder(this@MainActivity)
                                        .setTitle("Delete Task")
                                        .setMessage("Are you sure you want to delete \"${task.title}\"?")
                                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteTask(task) }
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

    /**
     * Updates [currentTasks] with the latest emission from the database and refreshes the
     * displayed list using the current search query.
     *
     * @param tasks The full, up-to-date list of tasks from the database.
     */
    private fun renderTasks(tasks: List<Task>) {
        currentTasks = tasks
        displayTasks(searchBar.text.toString())
    }
}