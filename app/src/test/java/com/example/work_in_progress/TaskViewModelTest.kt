package com.example.work_in_progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
}