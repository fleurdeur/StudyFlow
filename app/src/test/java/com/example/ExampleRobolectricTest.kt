package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.nativebridge.NativeBridge
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StudyFlow", appName)
  }

  @Test
  fun `test auto categorization of course code and topic`() {
    val result = NativeBridge.autoCategorize("CS201 Homework", "Implement binary tree traversal and quicksort algorithms")
    assertEquals("CS201", result.courseCode)
    assertEquals("Computer Science", result.category)
    assertTrue(result.confidence > 0.8)
  }

  @Test
  fun `test document parsing pipeline`() {
    val doc = NativeBridge.parseDocument("BIO105_Lecture.pdf", "Cellular respiration produces ATP via mitochondria.")
    assertEquals("BIO105", doc.courseCode)
    assertTrue(doc.cards.isNotEmpty())
  }

  @Test
  fun `test task creation and deletion in AppViewModel`() {
    val viewModel = AppViewModel()
    assertTrue(viewModel.uiState.value.tasks.isEmpty())

    viewModel.addTask("Test Lab", "Write report", "BIO105", TaskPriority.HIGH, TaskType.ASSIGNMENT, 1)
    assertEquals(1, viewModel.uiState.value.tasks.size)

    val taskId = viewModel.uiState.value.tasks.first().id
    viewModel.deleteTask(taskId)
    assertTrue(viewModel.uiState.value.tasks.isEmpty())
  }

  @Test
  fun `test note creation and deletion in AppViewModel`() {
    val viewModel = AppViewModel()
    assertTrue(viewModel.uiState.value.notes.isEmpty())

    viewModel.saveNote("Trees Note", "Binary search tree concepts", "CS201", listOf("Trees"))
    assertEquals(1, viewModel.uiState.value.notes.size)

    val noteId = viewModel.uiState.value.notes.first().id
    viewModel.deleteNote(noteId)
    assertTrue(viewModel.uiState.value.notes.isEmpty())
  }

  @Test
  fun `test real date time calculations`() {
    val today = RealDateTimeUtils.getToday()
    assertNotNull(today)
    val weekDays = RealDateTimeUtils.getCurrentWeekDays()
    assertEquals(7, weekDays.size)
    assertTrue(weekDays.any { it.isToday })
  }

  @Test
  fun `test task sorting options`() {
    val viewModel = AppViewModel()
    viewModel.addTask("A Task", "", "CS201", TaskPriority.LOW, TaskType.ASSIGNMENT, 5)
    viewModel.addTask("B Task", "", "BIO105", TaskPriority.URGENT, TaskType.ASSIGNMENT, 1)

    viewModel.setTaskSortOrder(TaskSortOrder.PRIORITY_DESC)
    assertEquals(TaskSortOrder.PRIORITY_DESC, viewModel.uiState.value.taskSortOrder)

    viewModel.setTaskSortOrder(TaskSortOrder.DUE_DATE_ASC)
    assertEquals(TaskSortOrder.DUE_DATE_ASC, viewModel.uiState.value.taskSortOrder)
  }
}
