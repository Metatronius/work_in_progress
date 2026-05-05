package com.example.work_in_progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.work_in_progress.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskViewModel
    private val repository: TaskRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.allTasks).thenReturn(flowOf(emptyList()))
        viewModel = TaskViewModel(repository)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun addTaskTest() = runTest {
        val params = TaskParams("Test")

        viewModel.addTask(params)

        verify(repository).insert(argThat { title == "Test" })
    }

    @Test
    fun deleteTaskTest() = runTest {
        val task = Task(id = 1, title = "Delete Me")

        viewModel.deleteTask(task)
        verify(repository).delete(task)
    }

    @Test
    fun deleteByIdTest() = runTest {
        val taskId = 5
        val task = Task(id = taskId, title = "Delete Me")
        whenever(repository.getTaskById(taskId)).thenReturn(task)

        viewModel.deleteTaskById(taskId)

        verify(repository).delete(task)
    }

    @Test
    fun addTaskTitleValidation() = runTest {
        val params = TaskParams(title = "")
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }

    @Test
    fun addTaskInvalidPriority() = runTest {
        val params = TaskParams(title = "Valid Title", priority = 99)
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }

    // completeTask

    @Test
    fun completeTask_togglesProgressFrom0To1() = runTest {
        val task = Task(id = 1, title = "Task", progress = 0)
        viewModel.completeTask(task)
        verify(repository).update(task.copy(progress = 1))
    }

    @Test
    fun completeTask_togglesProgressFrom1To0() = runTest {
        val task = Task(id = 1, title = "Task", progress = 1)
        viewModel.completeTask(task)
        verify(repository).update(task.copy(progress = 0))
    }

    // addTask title-length boundary

    @Test
    fun addTask_titleExactly30Chars_succeeds() = runTest {
        val title = "a".repeat(30)
        viewModel.addTask(TaskParams(title = title))
        verify(repository).insert(argThat { this.title == title })
    }

    @Test
    fun addTask_titleExceeds30Chars_throws() = runTest {
        val params = TaskParams(title = "a".repeat(31))
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }

    // addTask priority boundaries

    @Test
    fun addTask_negativePriority_throws() = runTest {
        val params = TaskParams(title = "Valid", priority = -1)
        Assert.assertThrows(IllegalArgumentException::class.java) {
            viewModel.addTask(params)
        }
        verify(repository, never()).insert(any())
    }

    @Test
    fun addTask_allValidPriorities_succeed() = runTest {
        for (priority in 0..3) {
            viewModel.addTask(TaskParams(title = "Task", priority = priority))
        }
        verify(repository, times(4)).insert(any())
    }

    // deleteTaskById – task not found

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
            priority = 2,
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

    @Test
    fun taskViewModelFactory_createsTaskViewModel() {
        val factory = TaskViewModelFactory(repository)
        val vm = factory.create(TaskViewModel::class.java)
        Assert.assertNotNull(vm)
        Assert.assertTrue(vm is TaskViewModel)
    }

    @Test
    fun taskViewModelFactory_unknownClass_throws() {
        val factory = TaskViewModelFactory(repository)
        Assert.assertThrows(IllegalArgumentException::class.java) {
            factory.create(OtherViewModel::class.java)
        }
    }
}