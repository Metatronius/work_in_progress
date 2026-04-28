package com.example.work_in_progress

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetail : AppCompatActivity() {

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
