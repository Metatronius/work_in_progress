package com.example.work_in_progress

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddTask : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val taskTitle = findViewById<EditText>(R.id.taskTitle)

        saveButton.setOnClickListener {

            val title = taskTitle.text.toString()

            val resultIntent = Intent()
            resultIntent.putExtra("TASK_TITLE", title)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}