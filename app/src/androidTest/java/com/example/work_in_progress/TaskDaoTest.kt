package com.example.work_in_progress

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.work_in_progress.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var taskDao: TaskDao
    private lateinit var db: AppDatabase

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        taskDao = db.taskDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeTaskAndReadInList() = runBlocking {
        val task = Task(title = "Test Task")
        taskDao.insertTask(task)

        val allTasks = taskDao.getAllTasks().first()
        assert(allTasks[0].title == task.title)
    }

    @Test
    fun deleteTask() = runBlocking {
        val task = Task(id = 2, title = "Delete Me")
        taskDao.insertTask(task)
        taskDao.deleteTask(task)

        val allTasks = taskDao.getAllTasks().first()

        assert(allTasks.isEmpty())
    }

    @Test
    fun getTaskById() = runBlocking {
        val task = Task(id = 2, title = "Get Me")
        taskDao.insertTask(task)
        val insertedTask = taskDao.getTaskById(task.id)

        assert(insertedTask !== null)
        assert(insertedTask?.title == task.title)
    }

    @Test
    fun updateTask_persistsChanges() = runBlocking {
        val task = Task(id = 3, title = "Original", notes = "Old notes")
        taskDao.insertTask(task)

        val updated = task.copy(title = "Updated", notes = "New notes")
        taskDao.updateTask(updated)

        val fetched = taskDao.getTaskById(3)
        assertEquals("Updated", fetched?.title)
        assertEquals("New notes", fetched?.notes)
    }

    @Test
    fun getAllTasks_returnsInDescendingIdOrder() = runBlocking {
        taskDao.insertTask(Task(id = 1, title = "First"))
        taskDao.insertTask(Task(id = 2, title = "Second"))
        taskDao.insertTask(Task(id = 3, title = "Third"))

        val allTasks = taskDao.getAllTasks().first()

        assertEquals(3, allTasks[0].id)
        assertEquals(2, allTasks[1].id)
        assertEquals(1, allTasks[2].id)
    }

    @Test
    fun getTaskById_returnsNullForMissingId() = runBlocking {
        val result = taskDao.getTaskById(999)
        assertNull(result)
    }

    @Test
    fun insertTask_replacesOnConflict() = runBlocking {
        val task = Task(id = 10, title = "Original")
        taskDao.insertTask(task)

        val replacement = Task(id = 10, title = "Replaced")
        taskDao.insertTask(replacement)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Replaced", allTasks[0].title)
    }
}