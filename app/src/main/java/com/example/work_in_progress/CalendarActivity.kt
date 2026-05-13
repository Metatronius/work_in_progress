package com.example.work_in_progress

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var taskList: ArrayList<Task>
    private var isWeekly = false
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private val formatter = SimpleDateFormat("M/d/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val calendarView =
            findViewById<CalendarView>(R.id.calendarView)

        val selectedDateTasks =
            findViewById<TextView>(R.id.selectedDateTasks)

        val toggleButton =
            findViewById<Button>(R.id.toggleViewButton)

        val weekContainer =
            findViewById<LinearLayout>(R.id.weekContainer)

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        taskList =
            intent.getParcelableArrayListExtra("TASK_LIST")
                ?: arrayListOf()

        bottomNav.selectedItemId = R.id.nav_calendar

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    finish()
                    true
                }

                R.id.nav_calendar -> true

                else -> false
            }
        }

        // date click
        calendarView.setOnDateChangeListener { _, year, month, day ->

            selectedCalendar.set(year, month, day)

            val selectedDate =
                formatDate(selectedCalendar)

            val tasksForDay = taskList.filter {

                normalizeDate(it.date) == selectedDate
            }

            if (tasksForDay.isNotEmpty()) {

                val taskText =
                    tasksForDay.joinToString("\n") {
                        "• ${it.title}"
                    }

                selectedDateTasks.text =
                    "Tasks for $selectedDate\n$taskText"

            } else {

                selectedDateTasks.text =
                    "Tasks for $selectedDate\n(No tasks)"
            }

            if (isWeekly) {
                showWeeklyTasks(weekContainer)
            }
        }

        toggleButton.setOnClickListener {

            isWeekly = !isWeekly

            if (isWeekly) {

                calendarView.visibility = View.GONE
                weekContainer.visibility = View.VISIBLE
                toggleButton.text = "Switch to Monthly"

                showWeeklyTasks(weekContainer)

            } else {

                calendarView.visibility = View.VISIBLE
                weekContainer.visibility = View.GONE
                toggleButton.text = "Switch to Weekly"
            }
        }
    }

    private fun showWeeklyTasks(container: LinearLayout) {

        container.removeAllViews()

        val weekStart =
            selectedCalendar.clone() as Calendar

        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val daysOfWeek = listOf(
            "Sun",
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
        )

        for (i in 0..6) {

            val dayCalendar =
                weekStart.clone() as Calendar

            dayCalendar.add(Calendar.DAY_OF_MONTH, i)

            val dateString =
                formatDate(dayCalendar)

            val tasksForDay =
                taskList.filter {

                    normalizeDate(it.date) == dateString
                }

            val dayLayout = LinearLayout(this)
            dayLayout.orientation = LinearLayout.VERTICAL
            dayLayout.setPadding(12, 12, 12, 12)

            val dayTitle = TextView(this)

            if (tasksForDay.isNotEmpty()) {

                dayTitle.text =
                    "🔵 ${daysOfWeek[i]} ($dateString)"

                dayTitle.setTextColor(
                    Color.parseColor("#2196F3")
                )

            } else {

                dayTitle.text =
                    "${daysOfWeek[i]} ($dateString)"
            }

            dayTitle.textSize = 16f

            dayLayout.addView(dayTitle)

            if (tasksForDay.isEmpty()) {

                val emptyText = TextView(this)
                emptyText.text = "No tasks"

                dayLayout.addView(emptyText)

            } else {

                for (task in tasksForDay) {

                    val taskText = TextView(this)

                    taskText.text =
                        "• ${task.title}"

                    taskText.textSize = 14f
                    taskText.setPadding(8, 2, 0, 2)

                    dayLayout.addView(taskText)
                }
            }

            container.addView(dayLayout)
        }
    }

    private fun formatDate(calendar: Calendar): String {
        return formatter.format(calendar.time)
    }

    private fun normalizeDate(date: String): String {

        return date.replaceFirst("^0".toRegex(), "")
            .replace("/0", "/")
    }
}

