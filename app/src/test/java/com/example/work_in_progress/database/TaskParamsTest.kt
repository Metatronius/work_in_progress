// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress.database

import com.example.work_in_progress.util.Priority
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for the [TaskParams] value object, covering instantiation and field defaults.
 */
class TaskParamsTest {

    @Test
    fun taskParams_titleRequired() {
        val params = TaskParams(title = "My Task")
        Assert.assertEquals("My Task", params.title)
    }

    @Test
    fun taskParams_notesDefault() {
        val params = TaskParams(title = "Test")
        Assert.assertEquals("", params.notes)
    }

    @Test
    fun taskParams_notesCanBeSet() {
        val params = TaskParams(title = "Test", notes = "Some notes")
        Assert.assertEquals("Some notes", params.notes)
    }

    @Test
    fun taskParams_priorityDefault() {
        val params = TaskParams(title = "Test")
        Assert.assertEquals(Priority.NONE, params.priority)
    }

    @Test
    fun taskParams_priorityCanBeSet() {
        val params = TaskParams(title = "Test", priority = Priority.HIGH)
        Assert.assertEquals(Priority.HIGH, params.priority)
    }

    @Test
    fun taskParams_dueDefault() {
        val params = TaskParams(title = "Test")
        Assert.assertNull(params.due)
    }

    @Test
    fun taskParams_dueCanBeSet() {
        val params = TaskParams(title = "Test", due = "5/15/2025")
        Assert.assertEquals("5/15/2025", params.due)
    }

    @Test
    fun taskParams_remindDefault() {
        val params = TaskParams(title = "Test")
        Assert.assertFalse(params.remind)
    }

    @Test
    fun taskParams_remindCanBeSet() {
        val params = TaskParams(title = "Test", remind = true)
        Assert.assertTrue(params.remind)
    }

    @Test
    fun taskParams_progressDefault() {
        val params = TaskParams(title = "Test")
        Assert.assertEquals(0, params.progress)
    }

    @Test
    fun taskParams_progressCanBeSet() {
        val params = TaskParams(title = "Test", progress = 1)
        Assert.assertEquals(1, params.progress)
    }

    @Test
    fun taskParams_allFieldsCanBeSet() {
        val params = TaskParams(
            title = "Full Task",
            notes = "With notes",
            priority = Priority.MEDIUM,
            due = "3/20/2025",
            remind = true,
            progress = 1
        )
        Assert.assertEquals("Full Task", params.title)
        Assert.assertEquals("With notes", params.notes)
        Assert.assertEquals(Priority.MEDIUM, params.priority)
        Assert.assertEquals("3/20/2025", params.due)
        Assert.assertTrue(params.remind)
        Assert.assertEquals(1, params.progress)
    }

    @Test
    fun taskParams_equality() {
        val params1 = TaskParams(title = "Test", priority = Priority.LOW)
        val params2 = TaskParams(title = "Test", priority = Priority.LOW)
        Assert.assertEquals(params1, params2)
    }

    @Test
    fun taskParams_inequality_differentTitle() {
        val params1 = TaskParams(title = "Test1")
        val params2 = TaskParams(title = "Test2")
        Assert.assertNotEquals(params1, params2)
    }

    @Test
    fun taskParams_inequality_differentPriority() {
        val params1 = TaskParams(title = "Test", priority = Priority.LOW)
        val params2 = TaskParams(title = "Test", priority = Priority.HIGH)
        Assert.assertNotEquals(params1, params2)
    }
}
