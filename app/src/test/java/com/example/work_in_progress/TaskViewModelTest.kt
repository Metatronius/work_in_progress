/** Unit tests for [TaskViewModel] using coroutine test utilities and Mockito mocks. */
package com.example.work_in_progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.work_in_progress.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.kotlin.*

/**
 * Unit-test suite for [TaskViewModel], verifying task operations and input validation
 * using a mocked [TaskRepository] and a synchronous coroutine dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskViewModel
    private val repository: TaskRepository = mock()

    /** Initialises the test dispatcher, mock repository, and ViewModel before each test. */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.allTasks).thenReturn(flowOf(emptyList()))
        viewModel = TaskViewModel(repository)
    }

    /** Resets the main dispatcher to avoid leaking state between tests. */
    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    /** Verifies that [TaskViewModel.addTask] inserts a task with the expected title. */
    @Test
    fun addTaskTest() = runTest {
        val params = TaskParams("Test")

        viewModel.addTask(params)

        verify(repository).insert(argThat { title == "Test" })
    }

    /** Verifies that [TaskViewModel.deleteTask] delegates to the repository with the same task. */
    @Test
    fun deleteTaskTest() = runTest {
        val task = Task(id = 1, title = "Delete Me")

        viewModel.deleteTask(task)
        verify(repository).delete(task)
    }

    /** Verifies that [TaskViewModel.deleteTaskById] looks up the task and then deletes it. */
    @Test
    fun deleteByIdTest() = runTest {
        val taskId = 5
        val task = Task(id = taskId, title = "Delete Me")
        whenever(repository.getTaskById(taskId)).thenReturn(task)

        viewModel.deleteTaskById(taskId)

        verify(repository).delete(task)
    }

    /** Verifies that [TaskViewModel.addTask] rejects a blank title with [IllegalArgumentException]. */
    @Test
    fun addTaskTitleValidation() = runTest {
        val params = TaskParams(title = "")
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }

    /** Verifies that [TaskViewModel.addTask] rejects an out-of-range priority with [IllegalArgumentException]. */
    @Test
    fun addTaskInvalidPriority() = runTest {
        val params = TaskParams(title = "Valid Title", priority = 99)
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }
}