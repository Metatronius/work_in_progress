package com.example.work_in_progress.database

import com.example.work_in_progress.entities.Task
import org.junit.Assert
import org.junit.Test

/** Unit tests for [Task] entity edge cases including null handling and boundary conditions. */
class TaskEdgeCasesTest {

    @Test
    fun task_emptyNotes_createsSuccessfully() {
        val task = Task(id = 1, title = "Task", notes = "")
        assert(task.notes == "")
    }

    @Test
    fun task_whitespaceNotes_createsSuccessfully() {
        val task = Task(id = 1, title = "Task", notes = "   ")
        assert(task.notes == "   ")
    }

    @Test
    fun task_defaultNotes_isEmpty() {
        val task = Task(id = 1, title = "Task")
        assert(task.notes == "")
    }

    @Test
    fun task_longNotes_createsSuccessfully() {
        val longNotes = "a".repeat(10000)
        val task = Task(id = 1, title = "Task", notes = longNotes)
        assert(task.notes == longNotes)
    }

    @Test
    fun task_nullDueDate_createsSuccessfully() {
        val task = Task(id = 1, title = "Task", due = null)
        assert(task.due == null)
    }

    @Test
    fun task_emptyDueDate_createsSuccessfully() {
        val task = Task(id = 1, title = "Task", due = "")
        assert(task.due == "")
    }

    @Test
    fun task_validDueDate_createsSuccessfully() {
        val task = Task(id = 1, title = "Task", due = "05/20/2026")
        assert(task.due == "05/20/2026")
    }

    @Test
    fun task_nullDueDate_notEqualsEmptyDueDate() {
        val taskNull = Task(id = 1, title = "Task", due = null)
        val taskEmpty = Task(id = 1, title = "Task", due = "")
        assert(taskNull != taskEmpty)
    }

    @Test
    fun task_progressBoundary_0_isIncomplete() {
        val task = Task(id = 1, title = "Task", progress = 0)
        assert(task.progress == 0)
    }

    @Test
    fun task_progressBoundary_1_isComplete() {
        val task = Task(id = 1, title = "Task", progress = 1)
        assert(task.progress == 1)
    }

    @Test
    fun task_progressToggle_0to1() {
        val task = Task(id = 1, title = "Task", progress = 0)
        val toggled = task.copy(progress = (task.progress + 1) % 2)
        assert(toggled.progress == 1)
    }

    @Test
    fun task_progressToggle_1to0() {
        val task = Task(id = 1, title = "Task", progress = 1)
        val toggled = task.copy(progress = (task.progress + 1) % 2)
        assert(toggled.progress == 0)
    }

    @Test
    fun task_priorityBoundary_0_none() {
        val task = Task(id = 1, title = "Task", priority = 0)
        assert(task.priority == 0)
    }

    @Test
    fun task_priorityBoundary_3_high() {
        val task = Task(id = 1, title = "Task", priority = 3)
        assert(task.priority == 3)
    }

    @Test
    fun task_copyPreservesAllFields() {
        val original = Task(
            id = 1,
            title = "Original",
            notes = "Notes",
            priority = 2,
            due = "05/20/2026",
            remind = true,
            progress = 1,
            target = 1,
            created = "2026-05-19"
        )
        val copy = original.copy(title = "Updated")
        assert(copy.notes == original.notes)
        assert(copy.priority == original.priority)
        assert(copy.due == original.due)
        assert(copy.remind == original.remind)
        assert(copy.progress == original.progress)
    }

    @Test
    fun task_multipleTasksWithSameTitle_areDistinct() {
        val task1 = Task(id = 1, title = "Shared Title")
        val task2 = Task(id = 2, title = "Shared Title")
        assert(task1 != task2)
    }

    @Test
    fun task_defaultValues() {
        val task = Task(id = 1, title = "Task")
        assert(task.notes == "")
        assert(task.priority == 0)
        assert(task.due == null)
        assert(task.remind == false)
        assert(task.progress == 0)
        assert(task.target == 1)
    }

    @Test
    fun task_idAutoIncrement_startsAt0() {
        val task = Task(title = "Task")
        assert(task.id == 0)
    }

    @Test
    fun task_maxIdValue_doesNotOverflow() {
        val task = Task(id = Int.MAX_VALUE, title = "Task")
        assert(task.id == Int.MAX_VALUE)
    }

    @Test
    fun task_specialCharactersInTitle_createsSuccessfully() {
        val specialChars = "!@#\$%^&*()_+-=[]{}|;:',.<>?/~`"
        val task = Task(id = 1, title = specialChars)
        assert(task.title == specialChars)
    }

    @Test
    fun task_unicodeInTitle_createsSuccessfully() {
        val task = Task(id = 1, title = "任務 📝 Tâche")
        assert(task.title == "任務 📝 Tâche")
    }
}
