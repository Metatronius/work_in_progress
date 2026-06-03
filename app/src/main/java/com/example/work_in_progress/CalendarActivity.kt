package com.example.work_in_progress

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.work_in_progress.entities.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

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

        @Suppress("DEPRECATION")
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

                normalizeDate(it.due!!) == selectedDate
            }

            if (tasksForDay.isNotEmpty()) {

                val taskText =
                    tasksForDay.joinToString("\n") {
                        getString(R.string.task_bullet, it.title)
                    }

                selectedDateTasks.text =
                    getString(R.string.tasks_for_date, selectedDate, taskText)

            } else {

                selectedDateTasks.text =
                    getString(R.string.no_tasks_for_date, selectedDate)
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
                toggleButton.setText(R.string.switch_to_monthly)

                showWeeklyTasks(weekContainer)

            } else {

                calendarView.visibility = View.VISIBLE
                weekContainer.visibility = View.GONE
                toggleButton.setText(R.string.switch_to_weekly)
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
                    it.due != null && normalizeDate(it.due) == dateString
                }

            val dayLayout = LinearLayout(this)
            dayLayout.orientation = LinearLayout.VERTICAL
            dayLayout.setPadding(12, 12, 12, 12)

            val dayTitle = TextView(this)

            if (tasksForDay.isNotEmpty()) {

                dayTitle.text =
                    getString(R.string.day_title_with_tasks, daysOfWeek[i], dateString)

                dayTitle.setTextColor(
                    "#2196F3".toColorInt()
                )

            } else {

                dayTitle.text =
                    getString(R.string.day_title_no_tasks, daysOfWeek[i], dateString)
            }

            dayTitle.textSize = 16f

            dayLayout.addView(dayTitle)

            if (tasksForDay.isEmpty()) {

                val emptyText = TextView(this)
                emptyText.setText(R.string.no_tasks)

                dayLayout.addView(emptyText)

            } else {

                for (task in tasksForDay) {

                    val taskText = TextView(this)

                    taskText.text =
                        getString(R.string.task_bullet, task.title)

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

