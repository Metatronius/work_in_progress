/** Unit tests for [TaskViewModel] using coroutine test utilities and Mockito mocks. */
package com.example.work_in_progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.work_in_progress.database.*
import com.example.work_in_progress.util.Priority
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

    // completeTask

    /**
     * Tests the behavior of the `completeTask` function to ensure that it correctly toggles
     * the progress of a task from 1 to 0.
     *
     * This test verifies that when a task with progress of 1 is completed, the repository
     * is updated to reflect the task's progress being set to 0.
     */
    @Test
    fun completeTask_togglesProgressFrom0To1() = runTest {
        val task = Task(id = 1, title = "Task", progress = 0)
        viewModel.completeTask(task)
        verify(repository).update(task.copy(progress = 1))
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
        verify(repository).update(task.copy(progress = 0))
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
        verify(repository).insert(argThat { this.title == title })
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
        verify(repository, never()).insert(any())
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
        verify(repository, times(4)).insert(any())
    }

    /**
     * Tests the behavior of deleting a task by its ID when the task is not found.
     * It ensures that the `deleteTaskById` function does not call the delete operation
     * on the repository if the task with the specified ID does not exist.
     */
    @Test
    fun deleteTaskById_taskNotFound_doesNotCallDelete() = runTest {
        whenever(repository.getTaskById(99)).thenReturn(null)
        viewModel.deleteTaskById(99)
        verify(repository, never()).delete(any())
    }

    // addTask field mapping

    @Test
    fun addTask_setsAllFieldsCorrectly() = runTest {
        val params = TaskParams(
            title = "My Task",
            notes = "Some notes",
            priority = Priority.MEDIUM,
            due = "2025-01-01",
            remind = true,
            progress = 0
        )
        viewModel.addTask(params)
        verify(repository).insert(argThat {
            title == "My Task" &&
                notes == "Some notes" &&
                priority == 2 &&
                due == "2025-01-01" &&
                remind &&
                progress == 0 &&
                target == 1
        })
    }

    @Test
    fun addTask_invokesOnInsertedWithInsertedId() = runTest {
        whenever(repository.insert(any())).thenReturn(42L)
        var insertedId: Long? = null

        viewModel.addTask(TaskParams(title = "Task")) { id ->
            insertedId = id
        }

        Assert.assertEquals(42L, insertedId)
    }

    // allTasks LiveData

    @Test
    fun allTasks_exposesRepositoryData() {
        val tasks = listOf(Task(id = 1, title = "A"), Task(id = 2, title = "B"))
        whenever(repository.allTasks).thenReturn(flowOf(tasks))
        val vm = TaskViewModel(repository)
        var emitted: List<Task>? = null
        val observer = Observer<List<Task>> { emitted = it }
        vm.allTasks.observeForever(observer)
        Assert.assertEquals(tasks, emitted)
        vm.allTasks.removeObserver(observer)
    }

    // TaskViewModelFactory

    private class OtherViewModel : ViewModel()

    /**
     * Tests that the `TaskViewModelFactory` correctly creates an instance of `TaskViewModel`.
     *
     * This test verifies that when the `create` method is called with the `TaskViewModel` class,
     * a non-null instance of `TaskViewModel` is returned.
     *
     * @throws IllegalArgumentException if the class type provided is not `TaskViewModel`.
     */
    @Test
    fun taskViewModelFactory_createsTaskViewModel() {
        val factory = TaskViewModelFactory(repository)
        val vm = factory.create(TaskViewModel::class.java)
        Assert.assertNotNull(vm)
        Assert.assertTrue(vm is TaskViewModel)
    }

    /**
     * Tests that the `TaskViewModelFactory` throws an `IllegalArgumentException`
     * when attempting to create a `ViewModel` of an unknown class type.
     *
     * @throws IllegalArgumentException if the class type is not recognized by the factory.
     */
    @Test
    fun taskViewModelFactory_unknownClass_throws() {
        val factory = TaskViewModelFactory(repository)
        Assert.assertThrows(IllegalArgumentException::class.java) {
            factory.create(OtherViewModel::class.java)
        }
    }
}
