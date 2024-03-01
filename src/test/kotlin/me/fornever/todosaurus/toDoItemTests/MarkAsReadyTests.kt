package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.FakeRangeMarker
import me.fornever.todosaurus.services.ToDoItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class MarkAsReadyTests(private val newItem: String) {
    companion object {
        @JvmStatic
        @Parameters
        fun newItems()
            = arrayOf(
                "TODO",
                "todo",
                "text Todo",
                "ToDo",
                "Todo:text",
                "ToDo Text",
                "Todo:Text",
                "TODO    Text")
    }

    @Test
    fun `Should mark ToDo item as ready`() {
        // Arrange
        val expected = "TODO[#1]:"
        val sut = ToDoItem(FakeRangeMarker(newItem))

        // Act
        sut.markAsReady(1)

        // Assert
        assertTrue(sut.range.document.text.contains(expected))
    }

    fun `ToDo item should not be new`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(newItem))

        // Act
        sut.markAsReady(1)

        // Assert
        assertFalse(sut.isNew())
    }
}
