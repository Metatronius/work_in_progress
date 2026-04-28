package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var taskContainer: LinearLayout
    private lateinit var searchBar: EditText

    private val taskList = mutableListOf<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        taskContainer = findViewById(R.id.taskContainer)
        searchBar = findViewById(R.id.searchBar)

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivityForResult(intent, 1)
        }

        //  searchbar logic
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                displayTasks(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val task = mapOf(
                "TITLE" to (data?.getStringExtra("TITLE") ?: ""),
                "DATE" to (data?.getStringExtra("DATE") ?: ""),
                "PRIORITY" to (data?.getStringExtra("PRIORITY") ?: ""),
                "NOTES" to (data?.getStringExtra("NOTES") ?: "")
            )

            taskList.add(task)
            displayTasks(searchBar.text.toString())
        }
    }

    private fun displayTasks(query: String) {
        taskContainer.removeAllViews()

        for (task in taskList) {

            val title = task["TITLE"] ?: ""

            if (title.contains(query, ignoreCase = true)) {

                val rowLayout = LinearLayout(this)
                rowLayout.orientation = LinearLayout.HORIZONTAL
                rowLayout.setPadding(8, 8, 8, 8)

                val checkBox = CheckBox(this)

                val titleView = TextView(this)
                titleView.text = title
                titleView.textSize = 18f
                titleView.setPadding(8, 0, 0, 0)

                    //click
                titleView.setOnClickListener {
                    val intent = Intent(this, TaskDetail::class.java)
                    intent.putExtra("TITLE", task["TITLE"])
                    intent.putExtra("DATE", task["DATE"])
                    intent.putExtra("PRIORITY", task["PRIORITY"])
                    intent.putExtra("NOTES", task["NOTES"])
                    startActivity(intent)
                }

                rowLayout.addView(checkBox)
                rowLayout.addView(titleView)

                taskContainer.addView(rowLayout)
            }
        }
    }
}
