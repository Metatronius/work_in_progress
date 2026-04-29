package com.example.work_in_progress

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetail : AppCompatActivity() {

    /**
     * Initializes the task detail screen and populates each [android.widget.TextView]
     * with data received from the launching [android.content.Intent].
     *
     * @param savedInstanceState Previously saved instance state, or null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("TITLE")
        val date = intent.getStringExtra("DATE")
        val priority = intent.getStringExtra("PRIORITY")
        val notes = intent.getStringExtra("NOTES")

        val titleView = findViewById<TextView>(R.id.detailTitle)
        val dateView = findViewById<TextView>(R.id.detailDate)
        val priorityView = findViewById<TextView>(R.id.detailPriority)
        val notesView = findViewById<TextView>(R.id.detailNotes)

        titleView.text = title
        dateView.text = "Due: $date"
        priorityView.text = "Priority: $priority"
        notesView.text = "Notes: $notes"
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
