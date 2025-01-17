// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class MarkAsReportedTests(private val newItem: String) {
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
    fun `Should mark ToDo item as reported`() {
        // Arrange
        val expected = "TODO[#1]:"
        val sut = ToDoItem(FakeRangeMarker(newItem))

        // Act
        sut.markAsReported("1")

        // Assert
        assertTrue(sut.toDoRange.document.text.contains(expected))
    }

    @Test
    fun `ToDo item should not be new`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(newItem))

        // Act
        sut.markAsReported("1")

        // Assert
        assertFalse(sut.isNew)
    }
}
