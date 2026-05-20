// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "task_reminders",
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Task Due Soon")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(intent.getIntExtra("TASK_ID", 0), notification)
    }
}