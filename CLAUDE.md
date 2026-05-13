# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android Kotlin task management application. It allows users to create tasks with titles, notes, priority levels, due dates, and reminders, then view and manage them via a task list with search functionality.

**Target:** Android 31–35 | **Kotlin Version:** Official code style | **Compiled JVM:** Java 17

## Build & Development Commands

**Build the app:**
```bash
./gradlew build
```

**Run the app (requires connected device or emulator):**
```bash
./gradlew installDebug
# Then launch from device, or use:
adb shell am start -n com.example.work_in_progress/.MainActivity
```

**Run unit tests (on host machine):**
```bash
./gradlew test
```

**Run instrumented tests (on device/emulator):**
```bash
./gradlew connectedAndroidTest
```

**Run a single test class:**
```bash
./gradlew test --tests com.example.work_in_progress.ExampleUnitTest
./gradlew connectedAndroidTest --tests com.example.work_in_progress.ExampleInstrumentedTest
```

**Clean and rebuild:**
```bash
./gradlew clean build
```

**Lint and code quality check:**
```bash
./gradlew lint
```

## Architecture & Design Patterns

### High-Level Structure

The app follows an **MVVM-inspired architecture** with three key layers:

1. **Presentation Layer** (Activities)
   - `MainActivity`: Task list view with search filtering
   - `AddTask`: Create/edit task screen
   - `TaskDetail`: View task details
   - All activities use `TaskViewModel` to access data

2. **ViewModel Layer** (`TaskViewModel`, `TaskViewModelFactory`)
   - Lifecycle-aware data holder that survives configuration changes
   - Exposes `allTasks: LiveData<List<Task>>` for reactive UI updates
   - Provides business logic methods: `addTask()`, `completeTask()`
   - Uses `viewModelScope` for coroutine management (automatic cleanup on destroy)

3. **Data Access Layer** (`TaskRepository`)
   - Abstracts database operations
   - Mediates all reads/writes to Room
   - Injected into `TaskViewModel` via `TaskViewModelFactory`

4. **Database Layer** (Room)
   - `AppDatabase`: Room database singleton (double-checked locking, thread-safe)
   - `TaskDao`: Data access object with SQL queries
   - `Task`: JPA entity representing a task in the "tasks" table

### Data Flow

```
UI (Activities) → observe allTasks LiveData
                        ↓
                  TaskViewModel
                        ↓
                  TaskRepository
                        ↓
                  AppDatabase (Room) → SQLite
```

When the database changes, Room emits new data through the repository's Flow, which is converted to LiveData in the ViewModel. Activities observe this LiveData and automatically re-render.

### Key Design Decisions

- **Room Database Singleton**: Created once per app lifecycle with `getDatabase()` static method; prevents multiple DB instances
- **LiveData for UI reactivity**: Automatic observer cleanup and lifecycle awareness
- **TaskParams**: Separate parameter class for task creation (simplifies intent passing between activities)
- **In-memory cache** (`currentTasks` in MainActivity): Allows fast search filtering without repeated DB queries
- **Completion toggle** via `progress` field: 0 = incomplete, 1 = complete; toggled via `(progress + 1) % 2`

## Important Implementation Details

### Task Entity Fields

- `id`: Auto-generated primary key
- `title`: Task name
- `notes`: Optional detailed description
- `priority`: 0 = None, 1 = Low, 2 = Medium, 3 = High
- `created`: Timestamp (default: `Date().toString()`)
- `due`: Optional due-date string (nullable)
- `remind`: Boolean flag for reminders
- `progress`: 0 = incomplete, 1 = complete
- `target`: Target progress value (always 1 by default)

### Activity Communication

Activities pass task data via Intent extras (strings and booleans). Priority is encoded as numeric (1–3) internally but converted to labels ("Low", "Medium", "High") when passed between activities.

### Application Context Access

The `TaskApplication` extension provides a convenient way to access the repository from any `Context`:
```kotlin
val taskRepository = context.taskRepo
```

## Common Code Patterns

### Adding a Task

```kotlin
val params = TaskParams(title = "...", priority = 2, /* ... */)
viewModel.addTask(params)
```

### Observing Tasks in an Activity

```kotlin
viewModel.allTasks.observe(this) { tasks -> 
    // UI update with new task list
}
```

### Toggling Task Completion

```kotlin
viewModel.completeTask(task)
```

## Testing

- **Unit tests** go in `app/src/test/java/` and run on the host machine (JVM)
- **Instrumented tests** go in `app/src/androidTest/java/` and run on a device or emulator
- Existing test templates: `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`
- For database testing, consider creating a test Room database with `in-memory` mode: `.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()`

## Important Notes

- **Deprecated API**: The app currently uses `startActivityForResult()` which is deprecated; migrate to `ActivityResultLauncher` when convenient (noted in code comment)
- **Nullable Due Date**: The `due` field is nullable; always check before use
- **KSP Compilation**: Room code generation is handled by the Kotlin Symbol Processor (KSP); ensure KSP plugin is enabled in the build
- **Gradle Wrapper**: Use `./gradlew` instead of `gradle` to ensure consistent build environment
