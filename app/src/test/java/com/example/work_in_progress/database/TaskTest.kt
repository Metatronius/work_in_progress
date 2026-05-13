// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress.database

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for the [Task] entity, covering instantiation, field defaults,
 * and data class properties.
 */
class TaskTest {

    @Test
    fun task_createdWithId() {
        val task = Task(id = 1, title = "Test")
        Assert.assertEquals(1, task.id)
    }

    @Test
    fun task_titleRequired() {
        val task = Task(id = 1, title = "My Task")
        Assert.assertEquals("My Task", task.title)
    }

    @Test
    fun task_notesDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertEquals("", task.notes)
    }

    @Test
    fun task_notesCanBeSet() {
        val task = Task(id = 1, title = "Test", notes = "Some notes")
        Assert.assertEquals("Some notes", task.notes)
    }

    @Test
    fun task_priorityDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertEquals(0, task.priority)
    }

    @Test
    fun task_priorityCanBeSet() {
        val task = Task(id = 1, title = "Test", priority = 2)
        Assert.assertEquals(2, task.priority)
    }

    @Test
    fun task_dueDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertNull(task.due)
    }

    @Test
    fun task_dueCanBeSet() {
        val task = Task(id = 1, title = "Test", due = "5/15/2025")
        Assert.assertEquals("5/15/2025", task.due)
    }

    @Test
    fun task_remindDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertFalse(task.remind)
    }

    @Test
    fun task_remindCanBeSet() {
        val task = Task(id = 1, title = "Test", remind = true)
        Assert.assertTrue(task.remind)
    }

    @Test
    fun task_progressDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertEquals(0, task.progress)
    }

    @Test
    fun task_progressCanBeSet() {
        val task = Task(id = 1, title = "Test", progress = 1)
        Assert.assertEquals(1, task.progress)
    }

    @Test
    fun task_targetDefault() {
        val task = Task(id = 1, title = "Test")
        Assert.assertEquals(1, task.target)
    }

    @Test
    fun task_targetCanBeSet() {
        val task = Task(id = 1, title = "Test", target = 5)
        Assert.assertEquals(5, task.target)
    }

    @Test
    fun task_idDefault() {
        val task = Task(title = "Test")
        Assert.assertEquals(0, task.id)
    }

    @Test
    fun task_copy_updatesField() {
        val task = Task(id = 1, title = "Original", priority = 1)
        val updated = task.copy(title = "Updated")
        Assert.assertEquals("Updated", updated.title)
        Assert.assertEquals(1, updated.priority)
    }

    @Test
    fun task_copy_createsNewInstance() {
        val task1 = Task(id = 1, title = "Test")
        val task2 = task1.copy(id = 2)
        Assert.assertNotEquals(task1, task2)
    }

    @Test
    fun task_equality() {
        val task1 = Task(id = 1, title = "Test")
        val task2 = Task(id = 1, title = "Test")
        Assert.assertEquals(task1, task2)
    }

    @Test
    fun task_inequality_differentId() {
        val task1 = Task(id = 1, title = "Test")
        val task2 = Task(id = 2, title = "Test")
        Assert.assertNotEquals(task1, task2)
    }

    @Test
    fun task_inequality_differentTitle() {
        val task1 = Task(id = 1, title = "Test1")
        val task2 = Task(id = 1, title = "Test2")
        Assert.assertNotEquals(task1, task2)
    }
}
