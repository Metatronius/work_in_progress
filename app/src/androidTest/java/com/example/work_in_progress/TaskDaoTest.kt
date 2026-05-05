/** Instrumented integration tests for [TaskDao] using an in-memory Room database. */
package com.example.work_in_progress

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Instrumented tests for [com.example.work_in_progress.database.TaskDao] using an in-memory
 * Room database so tests are isolated and leave no on-disk state.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var taskDao: TaskDao
    private lateinit var db: AppDatabase

    /** Opens an in-memory database and retrieves the DAO before each test. */
    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        taskDao = db.taskDao()
    }

    /** Closes the in-memory database after each test to release resources. */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /** Inserts a task and verifies it appears in the full task list. */
    @Test
    fun writeTaskAndReadInList() = runBlocking {
        val task = Task(title = "Test Task")
        taskDao.insertTask(task)

        val allTasks = taskDao.getAllTasks().first()
        assert(allTasks[0].title == task.title)
    }

    /** Inserts a task, deletes it, then verifies the task list is empty. */
    @Test
    fun deleteTask() = runBlocking {
        val task = Task(id = 2, title = "Delete Me")
        taskDao.insertTask(task)
        taskDao.deleteTask(task)

        val allTasks = taskDao.getAllTasks().first()

        assert(allTasks.isEmpty())
    }

    /** Inserts a task and verifies it can be retrieved by its primary key. */
    @Test
    fun getTaskById() = runBlocking {
        val task = Task(id = 2, title = "Get Me")
        taskDao.insertTask(task)
        val insertedTask = taskDao.getTaskById(task.id)

        assert(insertedTask !== null)
        assert(insertedTask?.title == task.title)
    }
}