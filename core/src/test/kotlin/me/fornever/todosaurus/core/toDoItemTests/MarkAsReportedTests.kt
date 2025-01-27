// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import com.intellij.testFramework.junit5.TestApplication
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestApplication
class MarkAsReportedTests {
    companion object {
        @JvmStatic
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

    @ParameterizedTest
    @MethodSource("newItems")
    fun `Should mark ToDo item as reported`(newItem: String) {
        // Arrange
        val expected = "TODO[#1]:"
        val sut = ToDoItem(TodosaurusSettings.State.defaultState, FakeRangeMarker(newItem))

        // Act
        sut.markAsReported("1")

        // Assert
        assertTrue(sut.toDoRange.document.text.contains(expected))
    }

    @ParameterizedTest
    @MethodSource("newItems")
    fun `ToDo item should not be new`(newItem: String) {
        // Arrange
        val sut = ToDoItem(TodosaurusSettings.State.defaultState, FakeRangeMarker(newItem))

        // Act
        sut.markAsReported("1")

        // Assert
        assertFalse(sut.isNew)
    }
}
