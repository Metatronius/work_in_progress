package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Task(
    var title: String,
    var date: String,
    var priority: String,
    var notes: String,
    var isCompleted: Boolean = false
) : Parcelable

class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private lateinit var searchBar: EditText
    private lateinit var dueSoonPlaceholder: TextView

    private val taskList = ArrayList<Task>()
    private var currentDisplayList = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        taskContainer = findViewById(R.id.taskContainer)
        searchBar = findViewById(R.id.searchBar)
        dueSoonPlaceholder = findViewById(R.id.dueSoonPlaceholder)

        currentDisplayList = taskList

        addTaskButton.setOnClickListener {
            startActivityForResult(Intent(this, AddTask::class.java), 1)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterTasks(s.toString())
            }
        })

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true

                R.id.nav_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    intent.putParcelableArrayListExtra("TASK_LIST", taskList)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        refreshTasks()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) return

        // delete task
        if (resultCode == 3) {

            val pos = data.getIntExtra("POSITION", -1)

            if (pos != -1 && pos < taskList.size) {

                taskList.removeAt(pos)

                refreshTasks()
            }

            return
        }

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {

            // add task
            1 -> {

                val task = Task(
                    data.getStringExtra("TITLE") ?: "",
                    data.getStringExtra("DATE") ?: "",
                    data.getStringExtra("PRIORITY") ?: "",
                    data.getStringExtra("NOTES") ?: "",
                    false
                )

                taskList.add(task)

                refreshTasks()
            }

            // edit task
            2 -> {

                val pos = data.getIntExtra("POSITION", -1)

                if (pos != -1 && pos < taskList.size) {

                    val completedState = taskList[pos].isCompleted

                    taskList[pos] = Task(
                        data.getStringExtra("TITLE") ?: "",
                        data.getStringExtra("DATE") ?: "",
                        data.getStringExtra("PRIORITY") ?: "",
                        data.getStringExtra("NOTES") ?: "",
                        completedState
                    )

                    refreshTasks()
                }
            }
        }
    }

    private fun refreshTasks() {

        val query = searchBar.text.toString()

        currentDisplayList = if (query.isEmpty()) {
            taskList
        } else {
            ArrayList(taskList.filter {
                it.title.lowercase().contains(query.lowercase())
            })
        }

        updateDueSoon()

        taskContainer.removeAllViews()

        for (task in currentDisplayList) {

            val realIndex = taskList.indexOf(task)

            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL

            val checkBox = CheckBox(this)
            checkBox.isChecked = task.isCompleted

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
            }

            val titleView = TextView(this)
            titleView.text = task.title
            titleView.textSize = 18f
            titleView.setPadding(8, 0, 0, 0)

            titleView.setOnClickListener {

                val intent = Intent(this, TaskDetail::class.java)

                intent.putExtra("TITLE", task.title)
                intent.putExtra("DATE", task.date)
                intent.putExtra("PRIORITY", task.priority)
                intent.putExtra("NOTES", task.notes)
                intent.putExtra("POSITION", realIndex)
                intent.putExtra("TASK_LIST", taskList)

                startActivityForResult(intent, 2)
            }

            rowLayout.addView(checkBox)
            rowLayout.addView(titleView)

            taskContainer.addView(rowLayout)
        }
    }

    private fun filterTasks(query: String) {
        refreshTasks()
    }

    private fun updateDueSoon() {

        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val today = Calendar.getInstance().time

        val dueSoonTasks = ArrayList<String>()

        for (task in taskList) {

            try {
                val taskDate = formatter.parse(task.date)

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
            } catch (e: Exception) {
            }
        }

        if (dueSoonTasks.isEmpty()) {
            dueSoonPlaceholder.text = "No tasks due soon"
        } else {
            dueSoonPlaceholder.text = dueSoonTasks.joinToString("\n")
        }
    }
}