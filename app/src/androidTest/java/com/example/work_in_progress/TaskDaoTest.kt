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
}