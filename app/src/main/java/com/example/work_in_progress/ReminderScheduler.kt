// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

object ReminderScheduler {

    fun schedule(context: Context, taskId: Int, taskTitle: String, dueDate: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_ID", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse the due date
        // TODO: BUG - Line 29: If date parsing fails, function silently returns without scheduling reminder. User won't know reminder wasn't set. Should log error or notify user.
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = try { format.parse(dueDate) } catch (e: Exception) { null } ?: return

        // remind 1 day before at 9am.
        // TODO: BUG - Line 31-38: Reminder time is hardcoded to 1 day before at 9am. User cannot customize. Consider if this matches expected behavior.
        val reminderTime = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
