package com.example.work_in_progress.database
import android.content.Context
import androidx.room.*

/**
 * Room database definition for the application.
 * Holds the [Task] entity and exposes access to [TaskDao].
 */
@Database(entities = [Task::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    /** Returns the DAO for task-related database operations. */
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton [AppDatabase] instance, creating it if necessary.
         * Thread-safe via double-checked locking with [synchronized].
         *
         * @param context Any [Context]; the application context is used internally
         *                to avoid leaking Activity contexts.
         * @return The application-wide [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}