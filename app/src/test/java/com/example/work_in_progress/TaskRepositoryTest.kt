package com.example.work_in_progress

import com.example.work_in_progress.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TaskRepositoryTest {
    private val taskDao: TaskDao = mock()
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(emptyList()))
        repository = TaskRepository(taskDao)
    }

    @Test
    fun insert_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Test")
        repository.insert(task)
        verify(taskDao).insertTask(task)
    }

    @Test
    fun update_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Updated")
        repository.update(task)
        verify(taskDao).updateTask(task)
    }

    @Test
    fun delete_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Delete Me")
        repository.delete(task)
        verify(taskDao).deleteTask(task)
    }

    @Test
    fun getTaskById_returnsTaskFromDao() = runTest {
        val task = Task(id = 3, title = "Task 3")
        whenever(taskDao.getTaskById(3)).thenReturn(task)
        val result = repository.getTaskById(3)
        Assert.assertEquals(task, result)
    }

    @Test
    fun getTaskById_returnsNullWhenNotFound() = runTest {
        whenever(taskDao.getTaskById(99)).thenReturn(null)
        val result = repository.getTaskById(99)
        Assert.assertNull(result)
    }

    @Test
    fun allTasks_returnsFlowFromDao() = runTest {
        val tasks = listOf(Task(id = 1, title = "A"), Task(id = 2, title = "B"))
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(tasks))
        val repo = TaskRepository(taskDao)
        val result = repo.allTasks.first()
        Assert.assertEquals(tasks, result)
    }
}
