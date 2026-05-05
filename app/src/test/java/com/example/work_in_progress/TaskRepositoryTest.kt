// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress

import com.example.work_in_progress.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for the [TaskRepository] class.
 *
 * This class sets up the necessary mocks and tests the interaction between
 * the repository and the data access object (DAO).
 */
class TaskRepositoryTest {
    private val taskDao: TaskDao = mock()
    private lateinit var repository: TaskRepository

    /**
     * Tests that the `insert` function of the `TaskRepository` correctly delegates the
     * task insertion to the `taskDao`.
     *
     * This test verifies that when a task is inserted into the repository,
     * the corresponding method in the data access object (DAO) is called with the
     * same task.
     *
     * @throws Exception if the test fails due to unexpected behavior.
     */
    @Before
    fun setup() {
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(emptyList()))
        repository = TaskRepository(taskDao)
    }

    /**
     * Tests that the `update` function of the repository correctly delegates the call to the `updateTask` method of the DAO.
     *
     * This function creates a `Task` object with an updated title and verifies that the DAO's `updateTask` method is invoked with the correct task.
     */
    @Test
    fun insert_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Test")
        repository.insert(task)
        verify(taskDao).insertTask(task)
    }

    /**
     * Tests that the `delete` function of the repository correctly delegates the call
     * to the `deleteTask` method of the task DAO.
     *
     * This test verifies that when a task is deleted from the repository,
     * the corresponding delete operation is performed on the task DAO.
     */
    @Test
    fun update_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Updated")
        repository.update(task)
        verify(taskDao).updateTask(task)
    }

    /**
     * Tests that the [repository] correctly retrieves a task by its ID from the [taskDao].
     *
     * This function sets up a mock for the [taskDao] to return a predefined task when queried
     * with a specific ID, and then verifies that the result from the repository matches the
     * expected task.
     *
     * @throws Exception if the task retrieval fails or does not match the expected result.
     * @return the task retrieved from the DAO.
     */
    @Test
    fun delete_delegatesToDao() = runTest {
        val task = Task(id = 1, title = "Delete Me")
        repository.delete(task)
        verify(taskDao).deleteTask(task)
    }

    /**
     * Tests the behavior of [repository.getTaskById] when a task is not found in the DAO.
     *
     * This test verifies that the method returns null when the task with the specified ID does not exist.
     */
    @Test
    fun getTaskById_returnsTaskFromDao() = runTest {
        val task = Task(id = 3, title = "Task 3")
        whenever(taskDao.getTaskById(3)).thenReturn(task)
        val result = repository.getTaskById(3)
        Assert.assertEquals(task, result)
    }

    /**
     * Tests that the `allTasks` function returns a flow of tasks from the DAO.
     *
     * This test mocks the `taskDao.getAllTasks()` method to return a predefined list of tasks,
     * and asserts that the result from the repository matches the expected list.
     */
    @Test
    fun getTaskById_returnsNullWhenNotFound() = runTest {
        whenever(taskDao.getTaskById(99)).thenReturn(null)
        val result = repository.getTaskById(99)
        Assert.assertNull(result)
    }

    /**
     * Tests that the `allTasks` function returns a Flow of tasks from the DAO.
     *
     * This test verifies that when `getAllTasks` is called on the mocked `taskDao`,
     * it returns a Flow containing the expected list of tasks.
     *
     * @throws Exception if the test fails due to an assertion error.
     */
    @Test
    fun allTasks_returnsFlowFromDao() = runTest {
        val tasks = listOf(Task(id = 1, title = "A"), Task(id = 2, title = "B"))
        whenever(taskDao.getAllTasks()).thenReturn(flowOf(tasks))
        val repo = TaskRepository(taskDao)
        val result = repo.allTasks.first()
        Assert.assertEquals(tasks, result)
    }
}
