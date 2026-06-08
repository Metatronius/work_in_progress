/** Unit tests for [TaskViewModel] using coroutine test utilities and MockK for suspend function mocking. */
package com.example.work_in_progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.work_in_progress.database.*
import com.example.work_in_progress.dtos.TaskParams
import com.example.work_in_progress.entities.Task
import com.example.work_in_progress.util.Priority
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*

/**
 * Unit-test suite for [TaskViewModel], verifying task operations and input validation
 * using MockK mocks and a synchronous coroutine dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskViewModel
    private val repository: TaskRepository = mockk()

    /** Initialises the test dispatcher, mock repository, and ViewModel before each test. */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.allTasks } returns flowOf(emptyList())
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.update(any()) } returns Unit
        coEvery { repository.delete(any()) } returns Unit
        coEvery { repository.getTaskById(any()) } returns null
        viewModel = TaskViewModel(repository)
    }

    /** Resets the main dispatcher to avoid leaking state between tests. */
    @After
    fun cleanup() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    /** Verifies that [TaskViewModel.addTask] inserts a task with the expected title. */
    @Test
    fun addTaskTest() = runTest {
        val params = TaskParams("Test")

        viewModel.addTask(params)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.insert(match { it.title == "Test" }) }
    }

    /** Verifies that [TaskViewModel.deleteTask] delegates to the repository with the same task. */
    @Test
    fun deleteTaskTest() = runTest {
        val task = Task(id = 1, title = "Delete Me")

        viewModel.deleteTask(task)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.delete(task) }
    }

    /** Verifies that [TaskViewModel.deleteTaskById] looks up the task and then deletes it. */
    @Test
    fun deleteByIdTest() = runTest {
        val taskId = 5
        val task = Task(id = taskId, title = "Delete Me")
        coEvery { repository.getTaskById(taskId) } returns task

        viewModel.deleteTaskById(taskId)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.delete(task) }
    }

    /** Verifies that [TaskViewModel.addTask] rejects a blank title with [IllegalArgumentException]. */
    @Test
    fun addTaskTitleValidation() = runTest {
        val params = TaskParams(title = "")
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    // completeTask

    /**
     * Tests the behavior of the `completeTask` function to ensure that it correctly toggles
     * the progress of a task from 0 to 1.
     *
     * This test verifies that when a task with progress of 0 is completed, the repository
     * is updated to reflect the task's progress being set to 1.
     */
    @Test
    fun completeTask_togglesProgressFrom0To1() = runTest {
        val task = Task(id = 1, title = "Task", progress = 0)
        viewModel.completeTask(task)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.update(task.copy(progress = 1)) }
    }

    /**
     * Tests the `completeTask` function to ensure that completing a task toggles its progress
     * from 1 to 0.
     *
     * This test verifies that when a task with a progress of 1 is completed, the repository
     * is called to update the task's progress to 0.
     */
    @Test
    fun completeTask_togglesProgressFrom1To0() = runTest {
        val task = Task(id = 1, title = "Task", progress = 1)
        viewModel.completeTask(task)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.update(task.copy(progress = 0)) }
    }

    /**
     * Tests the `addTask` function to ensure that adding a task with a title that is exactly
     * 30 characters long succeeds.
     *
     * This test verifies that when a task with a title of 30 characters is added, the repository
     * is called to insert the task with the correct title.
     */
    @Test
    fun addTask_titleExactly30Chars_succeeds() = runTest {
        val title = "a".repeat(30)
        viewModel.addTask(TaskParams(title = title))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.insert(match { it.title == title }) }
    }

    /**
     * Tests that adding a task with a title exceeding 30 characters throws an
     * IllegalArgumentException. It verifies that the repository's insert method
     * is never called when the title is invalid.
     *
     * @throws IllegalArgumentException if the title exceeds 30 characters.
     */
    @Test
    fun addTask_titleExceeds30Chars_throws() = runTest {
        val params = TaskParams(title = "a".repeat(31))
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    /**
     * Tests the behavior of adding tasks with all valid priority levels.
     * It verifies that the `addTask` function successfully inserts tasks
     * with priorities ranging from 0 to 3 into the repository.
     */
    @Test
    fun addTask_allValidPriorities_succeed() = runTest {
        for (priority in 0..3) {
            viewModel.addTask(TaskParams(title = "Task", priority = Priority.entries[priority]))
        }
        advanceUntilIdle()

        coVerify(exactly = 4) { repository.insert(any()) }
    }

    /**
     * Tests the behavior of deleting a task by its ID when the task is not found.
     * It ensures that the `deleteTaskById` function does not call the delete operation
     * on the repository if the task with the specified ID does not exist.
     */
    @Test
    fun deleteTaskById_taskNotFound_doesNotCallDelete() = runTest {
        coEvery { repository.getTaskById(99) } returns null
        viewModel.deleteTaskById(99)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.delete(any()) }
    }

    // addTask field mapping

    @Test
    fun addTask_setsAllFieldsCorrectly() = runTest {
        val params = TaskParams(
            title = "My Task",
            notes = "Some notes",
            priority = Priority.MEDIUM,
            due = "01/15/2025",
            remind = true,
            progress = 0
        )
        viewModel.addTask(params)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insert(match {
                it.title == "My Task" &&
                    it.notes == "Some notes" &&
                    it.priority == 2 &&
                    it.due == "01/15/2025" &&
                    it.remind &&
                    it.progress == 0 &&
                    it.target == 1
            })
        }
    }
}
