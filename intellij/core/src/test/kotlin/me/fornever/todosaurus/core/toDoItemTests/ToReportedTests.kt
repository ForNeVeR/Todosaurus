// SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import com.intellij.testFramework.junit5.TestApplication
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestApplication
class ToReportedTests {
    companion object {
        // IgnoreTODO-Start
        @JvmStatic
        fun newItems() = arrayOf(
            "TODO",
            "todo",
            "text Todo",
            "ToDo",
            "Todo:text",
            "ToDo Text",
            "Todo:Text",
            "TODO    Text"
        )
        // IgnoreTODO-End
    }

    // IgnoreTODO-Start
    @ParameterizedTest
    @MethodSource("newItems")
    fun `Should mark ToDo item as reported`(newItem: String) {
        // Arrange
        val expected = "TODO[#1]:"
        val sut = ToDoItem.fromRange(FakeRangeMarker(newItem), TodosaurusSettings.State.defaultState)

        if (sut !is ToDoItem.New)
            return fail()

        // Act
        sut.toReported("1")

        // Assert
        assertTrue(sut.toDoRange.document.text.contains(expected))
    }
    // IgnoreTODO-End

    @ParameterizedTest
    @MethodSource("newItems")
    fun `Should provide issue number after report`(newItem: String) {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(newItem), TodosaurusSettings.State.defaultState)

        if (sut !is ToDoItem.New)
            return fail()

        // Act
        val actual = sut.toReported("1")

        // Assert
        assertEquals("1", actual.issueNumber)
    }
}
