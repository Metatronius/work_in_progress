package com.example.work_in_progress.database

import com.example.work_in_progress.entities.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [TaskRepository] repository operations.
 * Tests the repository's wrapper methods around [TaskDao].
 * Uses mocking to isolate the repository from database implementation details.
 */
class TaskRepositoryTest {
    @Mock
    private lateinit var mockTaskDao: TaskDao

    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TaskRepository(mockTaskDao)
    }

    @Test
    fun repositoryInsert_shouldDelegateToDao() {
        runBlocking {
            val task = Task(
                title = "Test task",
                priority = 2,
                notes = "Test notes",
                due = "2026-05-15",
                remind = true
            )

            repository.insert(task)

            verify(mockTaskDao).insertTask(task)
        }
    }

    @Test
    fun repositoryUpdate_shouldDelegateToDao() {
        runBlocking {
            val task = Task(
                id = 1,
                title = "Updated task",
                priority = 3,
                progress = 1
            )

            repository.update(task)

            verify(mockTaskDao).updateTask(task)
        }
    }

    @Test
    fun repositoryDelete_shouldDelegateToDao() {
        runBlocking {
            val task = Task(
                id = 1,
                title = "Task to delete",
                priority = 1
            )

            repository.delete(task)

            verify(mockTaskDao).deleteTask(task)
        }
    }

    @Test
    fun repositoryAllTasks_shouldExposeAllTasksFlowFromDao() {
        runBlocking {
            val mockFlow = flowOf(listOf(Task(title = "Test task")))
            `when`(mockTaskDao.getAllTasks()).thenReturn(mockFlow)
            val repo = TaskRepository(mockTaskDao)

            val result = repo.allTasks.first()
            assertEquals(1, result.size)
            assertEquals("Test task", result[0].title)
        }
    }
}
